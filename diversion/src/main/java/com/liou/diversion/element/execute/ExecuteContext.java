package com.liou.diversion.element.execute;

import io.netty.channel.Channel;

public class ExecuteContext {

    public ExecuteContext(int sign, Channel channel) {
        if (sign == 0 || channel == null) {
            throw new IllegalArgumentException(String.format("uuid:%s, channel:%s", sign, channel));
        }
        this.sign = sign;
        this.channel = channel;
    }

    public final int sign;

    public final Channel channel;

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ExecuteContext) {
            ExecuteContext other = (ExecuteContext) obj;
            return other.sign == sign && other.channel.equals(channel);
        }
        return super.equals(obj);
    }
}
