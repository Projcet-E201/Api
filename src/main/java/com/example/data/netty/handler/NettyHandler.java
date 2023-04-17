package com.example.data.netty.handler;

import org.springframework.stereotype.Component;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class NettyHandler extends SimpleChannelInboundHandler<ByteBuf> {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		log.info("Channel active: {}", ctx.channel());
	}

	// 클라이언트와 연결되어 트래픽을 생성할 준비가 되었을 때 호출하는 메서드
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.info("Channel inactive: {}", ctx.channel());
	}

	// 데이터를 읽어들임.
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
		String receivedData = msg.toString(CharsetUtil.UTF_8);
		System.out.println("Received data: " + receivedData);
	}

	// 예외 발생시
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}