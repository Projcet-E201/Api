package com.example.data.netty.socket;

import org.springframework.stereotype.Component;

import com.example.data.netty.handler.NettyInboundHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.util.CharsetUtil;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NettyChannelInitializer extends ChannelInitializer<SocketChannel> {

	private final NettyInboundHandler nettyInboundHandler;

	@Override
	protected void initChannel(SocketChannel socketChannel) throws Exception {
		ChannelPipeline pipeline = socketChannel.pipeline();

		ByteBuf delimiter = Unpooled.copiedBuffer("\n", CharsetUtil.UTF_8);
		pipeline.addLast(new DelimiterBasedFrameDecoder(4098, delimiter)); // max frame length: 8192

		// Inbound 핸들러 등록
		pipeline.addLast(nettyInboundHandler);
	}
}
