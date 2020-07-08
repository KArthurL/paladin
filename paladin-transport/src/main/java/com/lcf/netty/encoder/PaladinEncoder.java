package com.lcf.netty.encoder;

import com.lcf.netty.common.PaladinMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * rpc消息的编码器
 *
 * @author k.arthur
 * @date 2020.7.9
 */
public class PaladinEncoder extends MessageToByteEncoder<PaladinMessage> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, PaladinMessage msg, ByteBuf byteBuf) throws Exception {
        byteBuf.writeInt(msg.getHeader());              //消息头+消息长度+类型+内容
        byteBuf.writeInt(msg.getContentLength());
        byteBuf.writeInt(msg.getType());
        byteBuf.writeBytes(msg.getContent());
    }
}
