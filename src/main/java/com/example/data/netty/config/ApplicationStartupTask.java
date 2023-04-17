package com.example.data.netty.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.example.data.netty.socket.NettyServerSocket;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ApplicationStartupTask implements ApplicationListener<ApplicationReadyEvent> {

	private final NettyServerSocket nettyServerSocket;
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		nettyServerSocket.start();
	}
}
