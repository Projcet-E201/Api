package com.example.data.netty.data;

import java.net.InetSocketAddress;

import javax.annotation.PreDestroy;

import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class DataNettyServerSocket {
	private final ServerBootstrap serverBootstrap;
	private final InetSocketAddress tcpPort;
	private Channel serverChannel;

	public void start() {

		// ChannelFuture: I/O operation의 결과나 상태를 제공하는 객체
		// 지정한 host, port로 소켓을 바인딩하고 incoming connections을 받도록 준비함

		// bind -> 지정된 tcpPort로 바인딩(서버가 해당 포트에 들어오는 연결 요청 수락준비됨)
		// sync -> bind가 반환하는 ChannelFuture 객체 sync 메서드로 바인딩 작업이 완료될때까지 현재 스레드 블로킹, 완료되면 작업의 결과 제공
		ChannelFuture serverChannelFuture = serverBootstrap.bind(tcpPort);

		// ChannelFutureListener를 사용하여 바인딩 작업이 완료되었을 때 처리할 이벤트를 등록
		serverChannelFuture.addListener((ChannelFuture future) -> {
			if (future.isSuccess()) {
				System.out.println("Server successfully bound to port " + tcpPort);
			} else {
				System.err.println("Failed to bind to port " + tcpPort + ". Cause: " + future.cause());
			}
		});

		// 서버 소켓 채널이 닫힐 때 발생하는 이벤트를 처리하는 ChannelFutureListener를 등록
		// channel -> ChannelFuture의 channel을 사용해 바인딩된 서버 소켓 채널가져옴
		// closeFuture -> 서버소켓 채널이 닫힐 때 발생하는 closeFuture 받음
		// sync -> 닫힐때 발생하는 ChannelFuture 객체의 sync 메서드 호출 채널이 닫힐 때 까지 현재 스레드 블로킹 (서버 소켓 채널이 닫히기 전까지 실행을 중단, 서버가 계속 실행되며 들어오는 연결처리) -> 제거
		// channel -> 서버 소켓채널이 닫힌 후 해당 채널 객체를 다기 사져옴
		serverChannel = serverChannelFuture.channel();
		serverChannel.closeFuture().addListener((ChannelFuture future) -> {
			System.out.println("Server channel closed.");
		});

	}

	// Bean을 제거하기 전에 해야할 작업이 있을 때 설정
	@PreDestroy
	public void stop() {
		if (serverChannel != null) {
			serverChannel.close();
			serverChannel.parent().closeFuture();
		}
	}
}