package com.liou.diversion.transport.netty;

import com.liou.diversion.container.Config;
import com.liou.diversion.element.execute.DiversionService;
import com.liou.diversion.node.AccessService;
import com.liou.diversion.node.DiversionNode;
import com.liou.diversion.transport.ChannelFactory;
import com.liou.diversion.transport.Charset;
import com.liou.diversion.transport.IoChannel;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

public class NettyChannelFactory implements ChannelFactory {

    private static final Logger logger = LoggerFactory.getLogger(NettyChannelFactory.class);

    private Bootstrap bootstrap;
    private EventLoopGroup bossElg;
    private EventLoopGroup ioElg;

    /**
     * i/o thread count
     */
    @Config("diversion.io.threadcount")
    private int ioThreadCount;

    /**
     * 连接最大尝试次数
     */
    @Config("diversion.io.attempts")
    private int connectAttempts;
    /**
     * 传输编码
     */
    @Config("diversion.io.charset")
    private Charset charset;
    /**
     * 写闲置时间（毫秒）
     */
    @Config("diversion.io.writidle")
    private int writerIdleTime;
    /**
     * 读闲置时间（毫秒）
     */
    @Config("diversion.io.readidle")
    private int readerIdleTime;

    private PacketChannelHandler packetChannelHandler;
    private ServerChannelHandler serverChannelHandler;
    private KeepAliveChannelHandler keepAliveChannelHandler;
    private WriteIdleChannelHandler writeIdleChannelHandler;
    private ReadIdleChannelHandler readIdleChannelHandler;
    private DiversionService diversionService;

    public NettyChannelFactory() {
    }

    @Override
    public void init() {
        ioElg = new NioEventLoopGroup(ioThreadCount);
        bossElg = new NioEventLoopGroup(1);
        bootstrap = new Bootstrap();
        bootstrap.group(ioElg).channel(NioSocketChannel.class);

        packetChannelHandler = new PacketChannelHandler();
        serverChannelHandler = new ServerChannelHandler(diversionService);
        keepAliveChannelHandler = new KeepAliveChannelHandler();
        writeIdleChannelHandler = new WriteIdleChannelHandler(charset);
        readIdleChannelHandler = new ReadIdleChannelHandler();
    }

    @Override
    public IoChannel createChannel(String host, int port, SocketAddress localAddress) throws IOException {
        Channel channel = connect(host, port, localAddress);
        return new NettyChannel(host, port, channel);
    }

    @Override
    public IoChannel reconnect(IoChannel channel, SocketAddress localAddress) throws IOException {
        NettyChannel nettyChannel = (NettyChannel) channel;
        String host = nettyChannel.getHost();
        int port = nettyChannel.getPort();
        Channel connected = connect(host, port, localAddress);
        nettyChannel.setChannel(connected);
        return nettyChannel;
    }

    private Channel connect(String host, int port, SocketAddress localAddress) throws IOException {
        bootstrap.handler(new ChannelInitializer<Channel>() {

            @Override
            protected void initChannel(Channel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addFirst(writeIdleChannelHandler);
                pipeline.addFirst(packetChannelHandler, serverChannelHandler, keepAliveChannelHandler);
                pipeline.addFirst(new IdleStateHandler(0, writerIdleTime, 0,
                        TimeUnit.MILLISECONDS));
            }

        });
        if (localAddress != null) {
            bootstrap.localAddress(localAddress);
        }
        Throwable cause = null;
        ChannelFuture future = null;
        for (int i = 0; i < connectAttempts; i++) {
            try {
                future = bootstrap.connect(host, port).sync();
                if (future.isSuccess()) {
                    Channel channel = future.channel();
                    return channel;
                } else {
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                cause = e;
            }
            logger.debug("try connect to {}:{} times {} failed", host, port, i + 1);
        }
        if (future != null && future.cause() != null) {
            cause = future.cause();
        }
        throw new IOException(String.format("fail to create channel with %s:%d", host, port), cause);
    }

    @Override
    public void acceptOn(int port, AccessService accessService) throws IOException, InterruptedException {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossElg, ioElg).channel(NioServerSocketChannel.class)
                .handler(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        logger.info("accepted channel {}", msg);
                        Channel channel = (Channel) msg;
                        InetSocketAddress inetSocketAddress = (InetSocketAddress) channel.remoteAddress();
                        String host = inetSocketAddress.getHostString();
                        int port = inetSocketAddress.getPort();
                        DiversionNode diversionNode = new DiversionNode(host, port);
                        diversionNode.channel(new NettyChannel(host, port, channel));
                        accessService.access(diversionNode);
                        ctx.fireChannelRead(msg);
                    }
                }).childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addFirst(readIdleChannelHandler);
                        pipeline.addFirst(packetChannelHandler, serverChannelHandler);
                        pipeline.addFirst(new IdleStateHandler(readerIdleTime, 0, 0, TimeUnit.MILLISECONDS));
                    }
                });
        serverBootstrap.bind(new InetSocketAddress(port));
    }

    @Override
    public void shutdown() {
        Future<?> shutdownGracefully = bossElg.shutdownGracefully();
        try {
            shutdownGracefully.await();
        } catch (Exception e) {
        }
        shutdownGracefully = ioElg.shutdownGracefully();
        try {
            shutdownGracefully.await();
        } catch (Exception e) {
        }
    }

    public void setDiversionService(DiversionService diversionService) {
        this.diversionService = diversionService;
    }

}