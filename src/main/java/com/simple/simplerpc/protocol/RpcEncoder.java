package com.simple.simplerpc.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author simple
 *
 * 编码
 */
public class RpcEncoder extends MessageToByteEncoder {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        byte[] data = HessianSerialize.serialize(o);
        assert data != null;
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);
    }

}
