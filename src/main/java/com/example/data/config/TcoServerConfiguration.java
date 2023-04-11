package com.example.data.config;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

@Configuration
public class TcoServerConfiguration {
	@Bean
	public DisposableServer disposableServer() {

		// 들어오는 Connection(클라이언트) 개체를 처리할 소비자 정의
		Consumer<Connection> connectionConsumer = connection -> connection
			// 서버와 연결된 클라이언트로부터 들어오는 데이터 바이드 배열로 수신
			.inbound()
			.receive()
			.asByteArray()
			// 수신된 바이트 배열을 UTF-8로 변환
			.map(bytes -> new String(bytes, StandardCharsets.UTF_8))
			// 개행 문자가 나타날 때까지 데이터를 누적한다.
			.windowUntil(str -> str.endsWith("\n"))
			// 누적된 데이터를 하나의 문자열로 변경해준다.
			.flatMap(window -> window.reduce((s1, s2) -> s1 + s2))
			// 수신된 데이터의 스트림 구독
			.subscribe(data -> {
				String[] split = data.split(" ");
				System.out.println(split.length);

				// To

				// Process the received data as needed
			});

		// TcpServer 인스턴스 생성
		return TcpServer.create()
			.host("127.0.0.1") // host
			.port(9000) // port
			.doOnConnection(connectionConsumer) // 이전에 정의된 connectionConsumer 등록
			.wiretap(true) // 디버깅에 사용할 유선 로깅 활성화
			.bindNow(); // 서버를 지정된 IP주소 및 포트에 바인딩하고 연결 수신 시작.
	}
}
