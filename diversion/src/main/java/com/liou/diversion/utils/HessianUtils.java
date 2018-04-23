package com.liou.diversion.utils;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;
import com.liou.diversion.element.Element;
import com.liou.diversion.transport.packet.Packet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * {@link Element}与{@link Packet}之间的互转
 *
 * @author liou
 */
public class HessianUtils {

    private static SerializerFactory serializerFactory = new SerializerFactory();

    public static Packet serialize(Object element, int requestSign) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(ByteUtils.toBytes(requestSign, true));
            Hessian2Output hessian2Output = new Hessian2Output(baos);
            hessian2Output.setSerializerFactory(serializerFactory);
            hessian2Output.writeObject(element);
            hessian2Output.close();
            byte[] bytes = baos.toByteArray();
            Packet packet = new Packet(bytes);
            packet.withUuid();
            return packet;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T deserialize(Packet packet) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(packet.payload());
            Hessian2Input hessian2Input = new Hessian2Input(bais);
            hessian2Input.setSerializerFactory(serializerFactory);
            T result = (T) hessian2Input.readObject();
            hessian2Input.close();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}