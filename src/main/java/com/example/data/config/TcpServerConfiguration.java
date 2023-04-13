package com.example.data.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.data.domain.motor.repository.MotorRepository;
import com.example.data.domain.sensorinfo.repository.SensorInfoRepository;
import com.example.data.entity.data.Motor;
import com.example.data.entity.data.SensorInfo;
import com.example.data.util.constants.TcpInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TcpServerConfiguration {

	private static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	private static final String OUTPUT_DIRECTORY = "server_directory/";

	private final MotorRepository motorRepository;
	private final SensorInfoRepository sensorInfoRepository;

	@Bean
	public DisposableServer sensorServer() {

		Consumer<Connection> connectionConsumer = connection -> connection
			.inbound()
			.receive()
			.asByteArray()
			.map(bytes -> new String(bytes, StandardCharsets.UTF_8))
			.flatMap(data -> Flux.fromArray(data.split("\n")))
			.subscribe(this::processMessage);

		return TcpServer.create()
			.host(TcpInfo.SENSOR_IP)
			.port(TcpInfo.SENSOR_PORT)
			.doOnConnection(connectionConsumer)
			.wiretap(true)
			.bindNow();
	}

	private void processMessage(String message) {
		if (!message.isEmpty()) {
			String[] dataPoints = message.split("\\s+");

			// Check if the dataPoints array has the expected length
			if (dataPoints.length % 3 == 0) {
				for (int i = 0; i < dataPoints.length; i += 3) {
					String sensorType = dataPoints[i];
					long sensorId = Long.parseLong(dataPoints[i + 1]);
					int value = Integer.parseInt(dataPoints[i + 2]);
					log.info("{} {} {}", sensorType, sensorId, value);

					SensorInfo sensorInfo = getOrCreateSensorInfo(sensorType, sensorId);
					saveMotorValue(sensorInfo, value);
				}
			} else {
				log.warn("Unexpected message format: {}", Arrays.toString(dataPoints));
			}
		}
	}

	private SensorInfo getOrCreateSensorInfo(String sensorType, long sensorId) {
		String compositeSensorId = sensorType + sensorId;
		return sensorInfoRepository.findBySensorId(compositeSensorId)
			.orElseGet(() -> sensorInfoRepository.save(SensorInfo.builder().sensorId(compositeSensorId).build()));
	}

	private void saveMotorValue(SensorInfo sensorInfo, int value) {
		motorRepository.save(Motor.builder().sensorInfo(sensorInfo).value(value).build());
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
