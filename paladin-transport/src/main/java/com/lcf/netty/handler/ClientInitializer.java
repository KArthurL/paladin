package com.lcf.netty.handler;

import com.lcf.netty.decoder.PaladinDecoder;
import com.lcf.netty.encoder.PaladinEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class ClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast(new PaladinEncoder())
                .addLast(new PaladinDecoder())
                .addLast(new PaladinClientHandler());
    }
}
