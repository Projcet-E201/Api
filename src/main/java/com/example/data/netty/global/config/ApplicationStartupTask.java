package com.example.data.netty.global.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.example.data.netty.data.DataNettyServerSocket;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ApplicationStartupTask implements ApplicationListener<ApplicationReadyEvent> {

	private final DataNettyServerSocket dataNettyServerSocket;
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		dataNettyServerSocket.start();
	}
}
