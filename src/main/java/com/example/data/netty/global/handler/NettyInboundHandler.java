package com.example.data.netty.global.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class NettyInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {

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
		log.info("Received data: {}", receivedData);

		// 데이터 파싱
		Map<String, String> parsedData = parseData(receivedData);

		// 데이터 타입 확인
		if (parsedData.containsKey("dataType") && "IMAGE".equals(parsedData.get("dataType"))) {
			// 이미지 데이터 처리
			processImageData(parsedData.get("dataValue"));
		}
	}

	// 예외 발생시
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		// ctx.close();
	}

	private Map<String, String> parseData(String receivedData) {
		String[] dataParts = receivedData.split(" ");
		Map<String, String> dataMap = new HashMap<>();

		if (dataParts.length == 3) {
			dataMap.put("serverName", dataParts[0]);
			dataMap.put("dataType", dataParts[1]);
			dataMap.put("dataValue", dataParts[2]);
		}

		return dataMap;
	}


	private void processImageData(String base64ImageData) {
		byte[] decodedImageData = Base64.getDecoder().decode(base64ImageData);
		// 이미지 파일 저장 경로와 파일 이름 설정
		String filePath = "/path/to/save/image/file";
		String fileName = "image.jpg";

		try (FileOutputStream fos = new FileOutputStream(new File(filePath, fileName))) {
			fos.write(decodedImageData);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}