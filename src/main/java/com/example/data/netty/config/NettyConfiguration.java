package com.example.data.netty.config;

import java.net.InetSocketAddress;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.data.netty.socket.NettyChannelInitializer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class NettyConfiguration {

	@Value("${netty.port}")
	private int port;
	@Value("${netty.boss-count}")
	private int bossCount;
	@Value("${netty.worker-count}")
	private int workerCount;
	@Value("${netty.keep-alive}")
	private boolean keepAlive;
	@Value("${netty.backlog}")
	private int backlog;

	// IP 소켓 주소(IP 주소, Port 번호)를 구현
	// 도메인 이름으로 객체 생성 가능
	@Bean
	public InetSocketAddress inetSocketAddress() {
		return new InetSocketAddress(port);
	}

	@Bean
	public ServerBootstrap serverBootstrap(NettyChannelInitializer nettyChannelInitializer) {
		// boss: incoming connection 수락하고, 수락한 connection을 worker에게 등록(register)
		// worker: boss 수락한 연결의 트래픽 관리
		NioEventLoopGroup bossGroup = new NioEventLoopGroup(bossCount);
		NioEventLoopGroup workerGroup = new NioEventLoopGroup(workerCount);

		// ServerBootstrap: 서버 설정을 도와주는 class
		ServerBootstrap b = new ServerBootstrap();

		b.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.handler(new LoggingHandler(LogLevel.DEBUG)) // 서버채널 핸들러(로깅 핸들러)
			.childHandler(nettyChannelInitializer); // 수락된 연결에 대한 초기화를 처리하는 핸들러

		b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		b.option(ChannelOption.SO_BACKLOG, backlog); // 서버가 받아들일 수 있는 연결의 최대 개수
		return b;
	}

}
