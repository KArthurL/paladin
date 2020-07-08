package com.lcf.netty.decoder;


import com.lcf.constants.RpcConstans;
import com.lcf.netty.common.PaladinMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * rpc消息的解码器
 *
 * @author k.arthur
 * @date 2020.7.9
 */
public class PaladinDecoder extends ByteToMessageDecoder {
    public final int BASE_LENGTH = 8;
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> list) throws Exception {
        if (in.readableBytes() >= BASE_LENGTH) {
            // 记录包头开始的index
            int beginReader;
            while (true) {
                // 获取包头开始的index
                beginReader = in.readerIndex();
                // 标记包头开始的index
                in.markReaderIndex();                                    //?????????
                // 读到协议的开始标志，结束while循环
                if (in.readInt() == RpcConstans.MESSAGE_HEAD) {
                    break;
                }

                // 未读到包头，跳过一个字节
                // 每次跳过一个字节后，再去读取包头信息的开始标记
                in.resetReaderIndex();
                in.readByte();

                // 当跳过一个字节后，数据包的长度又变的不满足，此时应该结束，等待后边数据流的到达
                if (in.readableBytes() < BASE_LENGTH) {
                    return;
                }
            }

            // 代码到这里，说明已经读到了报文标志

            // 消息长度
            int length = in.readInt();                       //去掉消息头，得到消息长度，得到类型???readInt????????
            // 判断请求数据包是否到齐
            if (in.readableBytes() < length) { // 数据不齐，回退读指针
                // 还原读指针
                in.readerIndex(beginReader);
                return;
            }

            // 至此，读到一条完整报文
            byte[] data = new byte[length - 4];
            System.out.println(length);
            int type = in.readInt();
            in.readBytes(data);
            PaladinMessage eflMessage = new PaladinMessage(length, type, data);
            list.add(eflMessage);
        }
    }
}
