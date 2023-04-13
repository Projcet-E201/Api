package com.example.data.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.data.util.constants.TcpInfo;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

@Configuration
@Slf4j
public class TcpServerConfiguration {

	private static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	private static final String OUTPUT_DIRECTORY = "server_directory/";

	@Bean
	public DisposableServer sensorServer() {

		// 들어오는 Connection(클라이언트) 개체를 처리할 소비자 정의
		Consumer<Connection> connectionConsumer = connection -> connection
			// 서버와 연결된 클라이언트로부터 들어오는 데이터 바이드 배열로 수신
			.inbound()
			.receive()
			.asByteArray()
			// 수신된 바이트 배열을 UTF-8로 변환
			.map(bytes -> new String(bytes, StandardCharsets.UTF_8))
			.flatMap(data -> Flux.fromArray(data.split("\n")))
			// 수신된 데이터의 스트림 구독
			.subscribe(message -> {
				if (!message.isEmpty()) {
					String[] dataPoints = message.split("\\s+");
					for (int i = 0; i < dataPoints.length; i += 3) {
						String sensorType = dataPoints[i];
						long sensorId = Long.parseLong(dataPoints[i + 1]);
						int value = Integer.parseInt(dataPoints[i + 2]);
						log.info("{} {} {}", sensorType, sensorId, value);
						// Add your logic to process the data here
					}
				}
			});

		// TcpServer 인스턴스 생성
		return TcpServer.create()
			.host(TcpInfo.SENSOR_IP) // host
			.port(TcpInfo.SENSOR_PORT) // port
			.doOnConnection(connectionConsumer) // 이전에 정의된 connectionConsumer 등록
			.wiretap(true) // 디버깅에 사용할 유선 로깅 활성화
			.bindNow(); // 서버를 지정된 IP주소 및 포트에 바인딩하고 연결 수신 시작.
	}
	@Bean
	public DisposableServer imgServer() {
		// Define a consumer to handle incoming Connection (client) objects
		Consumer<Connection> connectionConsumer = connection -> connection
				.inbound()
				.receive()
				.asByteArray()
				.subscribe(data -> {
					try {
						// Save received data as an image file
						saveReceivedImageFile(data);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
		// Create TcpServer instance
		return TcpServer.create()
				.host(TcpInfo.IMAGE_IP) // host
				.port(TcpInfo.IMAGE_PORT) // port
				.doOnConnection(connectionConsumer) // Register previously defined connectionConsumer
				.wiretap(true) // Enable wire logging for debugging
				.bindNow(); // Bind the server to the specified IP address and port, and start listening for connections.
	}
	@Bean
	public DisposableServer analogServer() {
		// Define a consumer to handle incoming Connection (client) objects
		Consumer<Connection> connectionConsumer = connection -> connection
			.inbound()
			.receive()
			.asByteArray()
			.collectList()
			.subscribe(data -> {
				try {
					// Save received data as a ZIP file
					saveReceivedZipFile(data);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

		// Create TcpServer instance
		return TcpServer.create()
			.host(TcpInfo.ANALOG_IP) // host
			.port(TcpInfo.ANALOG_PORT) // port
			.doOnConnection(connectionConsumer) // 이전에 정의된 connectionConsumer 등록
			.wiretap(true) // 디버깅에 사용할 유선 로깅 활성화
			.bindNow(); // 서버를 지정된 IP주소 및 포트에 바인딩하고 연결 수신 시작.
	}
	private void saveReceivedImageFile(byte[] data) throws IOException {
		String fileName = LocalDateTime.now().format(FILE_NAME_FORMATTER) + ".jpg";
		File outputFile = new File(OUTPUT_DIRECTORY, fileName);
		Files.createDirectories(outputFile.toPath().getParent());

		try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
			fileOutputStream.write(data);
		}
		System.out.println("Received file: " + outputFile.getAbsolutePath());
	}
	private void saveReceivedZipFile(List<byte[]> data) throws IOException {
		String fileName = LocalDateTime.now().format(FILE_NAME_FORMATTER) + ".zip";
		File outputFile = new File(OUTPUT_DIRECTORY, fileName);
		Files.createDirectories(outputFile.toPath().getParent());

		try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
			for (byte[] chunk : data) {
				fileOutputStream.write(chunk);
			}
		}
		System.out.println("Received file: " + outputFile.getAbsolutePath());
	}
}
