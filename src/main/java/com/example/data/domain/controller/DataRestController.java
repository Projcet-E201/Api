package com.example.data.domain.controller;

import com.example.data.util.constants.DataType;
import com.example.data.util.constants.TimeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;

import static java.lang.Math.abs;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DataRestController {

	private final InfluxDBClient influxDBClient;
	private static final Map<String, Integer> rule = new HashMap<String, Integer>() {{
		put("MOTOR", 300);
		put("VACUUM", 100);
		put("AIR_IN_KPA", 900);
		put("AIR_OUT_KPA", 900);
		put("AIR_OUT_MPA", 1);
		put("WATER", 4);
		put("ABRASION", 40);
		put("LOAD", 16);
		put("VELOCITY", 50000);
	}};

	@GetMapping("/machine/{data}/sensor")
	public String machineSensor(@PathVariable String data) throws Exception {

		String client = "CLIENT" + data;

		// 임시코드
		List<Map<String, Object>> outList = new ArrayList<>();
		Map<String, Object> outMap = new HashMap<>();

		// VELOCITY
		executeAndExtractData(client, DataType.VELOCITY, TimeInfo.MACHINE_SENSOR_VELOCITY_START, DataType.VELOCITY, outMap);

		// LOAD
		executeAndExtractData(client, DataType.LOAD, TimeInfo.MACHINE_SENSOR_LOAD_START, DataType.LOAD, outMap);

		// ABRASION
		executeAndExtractData(client, DataType.ABRASION, TimeInfo.MACHINE_SENSOR_ABRASION_START, DataType.ABRASION, outMap);

		// WATER
		executeAndExtractData(client, DataType.WATER, TimeInfo.MACHINE_SENSOR_WATER_START, DataType.WATER, outMap);


		//AIR_OUT_KPA
		executeAndExtractMinMaxData(client, DataType.AIR_OUT_MPA, TimeInfo.MACHINE_SENSOR_AIR_OUT_MPA_START, DataType.AIR_OUT_MPA,
				influxDBClient, outMap);

		//AIR_OUT_MPA
		executeAndExtractMinMaxData(client, DataType.AIR_OUT_KPA, TimeInfo.MACHINE_SENSOR_AIR_OUT_KPA_START, DataType.AIR_OUT_KPA,
				influxDBClient, outMap);

		//VACUUM
		executeAndExtractMinMaxData(client, DataType.VACUUM, TimeInfo.MACHINE_SENSOR_VACUUM_START, DataType.VACUUM, influxDBClient,
				outMap);

		//MOTOR
		executeAndExtractMinMaxData(client, DataType.MOTOR, TimeInfo.MACHINE_SENSOR_MOTOR_START, DataType.MOTOR, influxDBClient,
				outMap);


		// air_in_kpa
		String query = "from(bucket: \"week\")" +
				"|> range(start: -" + TimeInfo.MACHINE_SENSOR_AIR_IN_KPA_START + ", stop: now())" +
				"|> filter(fn: (r) => r[\"_measurement\"] == \"" + client + "\")" +
				"|> filter(fn: (r) => r[\"big_name\"] == \"AIR_IN_KPA\")" +
				"|> group(columns: [\"generate_time\"])" +
				"|> mean(column: \"_value\")" +
				"|> map(fn: (r) => ({value:r._value,time:r.generate_time }))" +
				"|> limit(n:10)";
		List<FluxTable>  tables = influxDBClient.getQueryApi().query(query, "semse");
		List<Map<String, Object>> recordsList = new ArrayList<>();
		Map<String, Object> recordMap = new HashMap<>();
		for (FluxTable table : tables) {
			for (FluxRecord record : table.getRecords()) {
				recordMap = new HashMap<>();
				Map<String, Object> valuesMap = record.getValues();
				recordMap.put("time", valuesMap.get("time"));
				recordMap.put("avg", valuesMap.get("value"));
				recordsList.add(recordMap);
				//                System.out.println("recordMap = " + recordMap);
			}
		}
		outMap.put("AIR_IN_KPA", recordsList);
		// 마무리
		outList.add(outMap);
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(outList);
	}

//	@GetMapping("/machine/{data}/state")
//	public List<Map<String, Object>> machineState(@PathVariable String data) {
//		String client = "CLIENT" + data;
//		String query = "from(bucket: \"day\")" +
//				"  |> range(start: -" + TimeInfo.MACHINE_STATE_START + ", stop: now())" +
//				"  |> filter(fn: (r) => r[\"_measurement\"] == \"" + client + "\")" +
//				"  |> group(columns:[\"name\"]) " +
//				"  |> last()" +
//				"  |> map(fn: (r) => ({value:r._value,time:r.generate_time,name:r.name}))";
//		List<FluxTable> tables = influxDBClient.getQueryApi().query(query, "semse");
//		List<Map<String, Object>> recordsList = new ArrayList<>();
//		Map<String, Object> recordMap = null;
//		String currentPrefix = "boolean";
//
//		for (FluxTable table : tables) {
//			for (FluxRecord record : table.getRecords()) {
//				Map<String, Object> valuesMap = record.getValues();
//				String name = valuesMap.get("name").toString();
//
//				// Name의 접두어를 가져옵니다.
//				String prefix = name.replaceAll("([a-zA-Z]*).*", "$1");
//
//				// Name의 접두어가 변경되었거나 recordMap이 아직 생성되지 않은 경우
//				if (recordMap == null || !prefix.equals(currentPrefix)) {
//
//					// 이전 recordMap에 대해 누락된 키를 추가합니다.
//					if (recordMap != null) {
//						for (int i = 1; i <= 10; i++) {
//							String key = currentPrefix + i;
//							recordMap.putIfAbsent(key, null);
//						}
//						recordsList.add(recordMap);
//					}
//					currentPrefix = prefix;
//					recordMap = new HashMap<>();
//				}
//				if (currentPrefix.equals("string")) {
//					Map<String, Object> innerValueMap = new HashMap<>();
//					innerValueMap.put("time", valuesMap.get("time"));
//					innerValueMap.put("value", valuesMap.get("value"));
//					recordMap.put(name, innerValueMap);
//				} else {
//					recordMap.put(name, valuesMap.get("value"));
//				}
//			}
//		}
//		// 마지막 recordMap에 대해 누락된 키를 추가합니다.
//		if (recordMap != null) {
//			for (int i = 1; i <= 10; i++) {
//				String key = currentPrefix + i;
//				recordMap.putIfAbsent(key, null);
//			}
//			recordsList.add(recordMap);
//			System.out.println("recordsList = " + recordsList);
//		}
//
//		return recordsList;
//	}

	@GetMapping("/machine/{data}/motor")
	public String machineMotor(@PathVariable String data) throws Exception {
		String client = "CLIENT" + data;
		String query = buildQuery(client, DataType.MOTOR, TimeInfo.MACHINE_MOTOR_START);
		return queryClientToJson(query);
	}

	@GetMapping("/machine/{data}/air_in_kpa")
	public String machineAirInKpa(@PathVariable String data) throws Exception {
		String client = "CLIENT" + data;
		String query = buildQuery(client, DataType.AIR_IN_KPA, TimeInfo.MACHINE_AIR_IN_KPA_START);
		return queryClientToJson(query);
	}

	@GetMapping("/machine/{data}/air_out_kpa")
	public String machineAirOutKpa(@PathVariable String data) throws Exception {
		String client = "CLIENT" + data;
		String query = buildQuery(client, DataType.AIR_OUT_KPA, TimeInfo.MACHINE_AIR_OUT_KPA_START);
		return queryClientToJson(query);
	}

	@GetMapping("/machine/{data}/air_out_mpa")
	public String machineAirOutMpa(@PathVariable String data) throws Exception {
		String client = "CLIENT" + data;
		String query = buildQuery(client, DataType.AIR_OUT_MPA, TimeInfo.MACHINE_AIR_OUT_MPA_START);
		return queryClientToJson(query);
	}

	@GetMapping("/machine/{data}/vacuum")
	public String machineVacuum(@PathVariable String data) throws Exception {
		String client = "CLIENT" + data;
		String query = buildQuery(client, DataType.VACUUM, TimeInfo.MACHINE_VACUUM_START);
		return queryClientToJson(query);
	}

	@GetMapping("/machine/{data}/water")
	public String machineWater(@PathVariable String data) throws Exception {
		String client = "CLIENT" + data;
		String query = buildQuery(client, DataType.WATER, TimeInfo.MACHINE_WATER_START);
		return queryClientToJson(query);
	}

	@GetMapping("/machine/{data}/abrasion")
	public String machineAbrasion(@PathVariable String data) throws Exception {
		String client = "CLIENT" + data;
		String query = buildQuery(client, DataType.ABRASION, TimeInfo.MACHINE_ABRASION_START);
		return queryClientToJson(query);
	}

	@GetMapping("/machine/{data}/load")
	public String machineLoad(@PathVariable String data) throws Exception {
		String client = "CLIENT" + data;
		String query = buildQuery(client, DataType.LOAD, TimeInfo.MACHINE_LOAD_START);
		return queryClientToJson(query);
	}

	@GetMapping("/machine/{data}/velocity")
	public String machineVelocity(@PathVariable String data) throws Exception {
		String client = "CLIENT" + data;
		String query = buildQuery(client, DataType.VELOCITY, TimeInfo.MACHINE_VELOCITY_START);
		return queryClientToJson(query);
	}

	@GetMapping("/main/machine")
	public String MainMachine() throws Exception {
		// 기기 각각의 최신값의 평균을 구하는 코드
		Map<String, Object> outMap = new HashMap<>();
		ObjectMapper objectMapper = new ObjectMapper();
		List<String> sensors = Arrays.asList("MOTOR", "AIR_IN_KPA", "AIR_OUT_KPA", "AIR_OUT_MPA", "LOAD", "VACUUM",
				"VELOCITY", "WATER", "ABRASION");

		List<CompletableFuture<Map<String, Object>>> futures = new ArrayList<>();

		for (int i = 1; i < 13; i++) {
			String client = "CLIENT" + i;
			futures.add(getSensorAveragesAsync(client, sensors));
		}

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

		for (int i = 1; i < 13; i++) {
			String client = "CLIENT" + i;
			outMap.put(client, futures.get(i - 1).get());
		}

		return "[" + objectMapper.writeValueAsString(outMap) + "]";
	}

	@GetMapping("/machine/{machine_number}/history/{sensor}/{sensor_id}/{start_time}/{end_time}")
	public String machineHistory(@PathVariable String machine_number,@PathVariable String sensor, @PathVariable String sensor_id ,@PathVariable String start_time, @PathVariable String end_time) throws Exception {
		// 프론트와 name 차이
		if (Objects.equals(sensor, "air-in")) {
			sensor = "air_in_kpa";
		} else if (Objects.equals(sensor, "air-out-kpa")) {
			sensor = "air_out_kpa";
		} else if (Objects.equals(sensor, "air-out-mpa")) {
			sensor = "air_out_mpa";
		} else if (Objects.equals(sensor, "rpm")) {
			sensor = "velocity";
		}

		String sensorUp = sensor.toUpperCase();
		String sensorLa = sensorUp + sensor_id;
		String sensorDo = sensor + sensor_id;
		String machine = "CLIENT" + machine_number;

		// 현재 시간 가져오기
		LocalDateTime nowTime = LocalDateTime.now();
		ZonedDateTime nowTimeUTC = nowTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC"));

		// UTC 시간 값을 ZonedDateTime으로 변환
		ZonedDateTime startDateTime = ZonedDateTime.parse(start_time, DateTimeFormatter.ISO_DATE_TIME);
		ZonedDateTime endDateTime = ZonedDateTime.parse(end_time, DateTimeFormatter.ISO_DATE_TIME);

		// 시간 차이 계산
		Duration startDifference = Duration.between(nowTimeUTC, startDateTime).abs();
		Duration endDifference = Duration.between(nowTimeUTC, endDateTime).abs();

		// 시간 간격 계산
		Duration interval = Duration.between(startDateTime, endDateTime).abs();
		long totalSeconds = interval.toSeconds();
		long desiredInterval = totalSeconds / 10; // 시간 간격을 10개로 나눔

		String startDifferenceInfluxDB = "-" + startDifference.toSeconds() + "s";
		String endDifferenceInfluxDB = "-" + endDifference.toSeconds() + "s";

		String query = "from(bucket: \"week\")" +
				"|> range(start: " + startDifferenceInfluxDB + ", stop: " + endDifferenceInfluxDB + ")" +
				"|> filter(fn: (r) => r[\"_measurement\"] == \"" + machine + "\")" +
				"|> filter(fn: (r) => r[\"name\"] == \"" + sensorLa + "\")" +
				"|> window(every: " + desiredInterval + "s)" + // 시간 간격 설정
				"|> last()" +
				"|> limit(n: 10)" + // 데이터 개수를 10개로 제한
				"|> map(fn: (r) => ({value:r._value,time:r.generate_time}))";

		// 쿼리 실행 및 결과 처리
		List<FluxTable> tables = influxDBClient.getQueryApi().query(query, "semse");
		Map<String, Object> result = new HashMap<>();
		List<Map<String, Object>> dataList = new ArrayList<>();
		result.put("id", sensorDo);
		result.put("data", dataList);
		for (FluxTable table : tables) {

			List<FluxRecord> records = table.getRecords();
			int size = records.size();
			int increment = 1;
			if (size>= 10) {
				increment = size / 10;
			}
			for (int i = 0; i < size; i++) {
				// 1/10배수번째 record만 처리
				if (i % increment == increment-1) {
					FluxRecord record = records.get(i);
					Map<String, Object> dataPoint = new HashMap<>();
					Map<String, Object> valuesMap = record.getValues();

					dataPoint.put("x", valuesMap.get("time").toString());
					dataPoint.put("y", valuesMap.get("value").toString());
					dataList.add(dataPoint);
				}
			}
		}
		System.out.println("dataList = " + dataList);

		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(result);
	}


	private String queryClientToJson(String query) throws JsonProcessingException {
		List<FluxTable> tables = influxDBClient.getQueryApi().query(query, "semse");
		//        System.out.println("tables = " + tables);
		List<Map<String, Object>> recordsList = new ArrayList<>();
		for (FluxTable table : tables) {
			for (FluxRecord record : table.getRecords()) {
				Map<String, Object> recordMap = new HashMap<>();
				Map<String, Object> valuesMap = record.getValues();
				recordMap.put("name", valuesMap.get("name"));
				recordMap.put("value", valuesMap.get("value"));
				Object timeSecond = (valuesMap.get("time"));
				recordMap.put("time", timeSecond);
				recordsList.add(recordMap);
			}
		}
		//        System.out.println("recordsList = " + recordsList);
		ObjectMapper mapper = new ObjectMapper();
		// Time 순으로 정렬
		Comparator<Map<String, Object>> timeNameComparator = new Comparator<>() {
			private final NameComparator nameComparator = new NameComparator();

			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				// 시간 우선순위로 정렬
				int timeComparison = ((Comparable)o1.get("time")).compareTo(o2.get("time"));
				if (timeComparison != 0) {
					return timeComparison;
				}
				// 시간이 같은 경우 이름으로 정렬
				String name1 = (String)o1.get("name");
				String name2 = (String)o2.get("name");
				return nameComparator.compare(name1, name2);
			}
		};
		// recordsList를 정렬
		recordsList.sort(timeNameComparator);
		return mapper.writeValueAsString(recordsList);
	}

	static class NameComparator implements Comparator<String> {
		@Override
		public int compare(String s1, String s2) {
			int len1 = s1.length(), len2 = s2.length();
			int i = 0, j = 0;
			while (i < len1 && j < len2) {
				// 숫자가 아닌 문자를 건너뜁니다.
				while (i < len1 && !Character.isDigit(s1.charAt(i)))
					i++;
				while (j < len2 && !Character.isDigit(s2.charAt(j)))
					j++;
				if (i == len1 || j == len2) {
					break;
				}
				int num1 = 0, num2 = 0;
				// 뒤에 붙은 숫자를 읽어옵니다.
				while (i < len1 && Character.isDigit(s1.charAt(i))) {
					num1 = num1 * 10 + (s1.charAt(i) - '0');
					i++;
				}
				while (j < len2 && Character.isDigit(s2.charAt(j))) {
					num2 = num2 * 10 + (s2.charAt(j) - '0');
					j++;
				}
				if (num1 != num2) {
					return num1 - num2;
				}
			}
			return s1.compareTo(s2);
		}
	}

	public CompletableFuture<Map<String, Object>> getSensorAveragesAsync(String client, List<String> sensors) {
		return CompletableFuture.supplyAsync(() -> {
			double score = 0.0;
			Map<String, Object> sensorMaps = new HashMap<>();
			for (String sensor : sensors) {
				String query = "from(bucket: \"week\")" +
						"  |> range(start: -" + TimeInfo.MAIN_MACHINE_START + ")" +
						"  |> filter(fn: (r) => r[\"_measurement\"] == \"" + client + "\")" +
						"  |> filter(fn: (r) => r[\"big_name\"] == \"" + sensor + "\")" +
						"  |> last()" +
						"  |> group(columns: [\"name\"])" +
						"  |> last()";
				int divideNo = rule.get(sensor);
				List<FluxTable> tables = influxDBClient.getQueryApi().query(query, "semse");

				if (!tables.isEmpty()) {
					FluxRecord record = tables.get(0).getRecords().get(0);
					if (record != null) {
						double value = Double.parseDouble(record.getValueByKey("_value").toString());
						DecimalFormat df = new DecimalFormat("#.##"); // 소수점 두 번째 자리까지 포맷 정의
						value = Double.parseDouble(df.format(value)); // 포맷 적용
						//70% 보다 낮으면 점수값을 높여 준다.
						double score_s = abs(value/divideNo);
						if (score_s <0.7) {
							score_s += 0.15;
						}
						score += score_s;
						sensorMaps.put(sensor, value);
					} else {
						log.info("N1o data for client: " + client + " sensor: " + sensor);
					}
				}
			}
			int scoreLa = 0; //초기화
			scoreLa = (int) Math.round(score*100/9);
			if (100 < scoreLa) {
				scoreLa = 100;
			} else if (scoreLa < 20) {
				scoreLa = 0;
			} else if (scoreLa < 50) {
				scoreLa -= 10;
			} else if (scoreLa > 60 && scoreLa < 90) {
				scoreLa += 6;
			}
			sensorMaps.put("SCORE", scoreLa);
			return sensorMaps;
		});
	}

	private void executeAndExtractData(String client, DataType sensorType, String timeStart, DataType metric, Map<String, Object> outMap) {
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("from(bucket: \"week\")")
				.append("|> range(start: -").append(timeStart).append(", stop: now())")
				.append("|> filter(fn: (r) => r[\"_measurement\"] == \"").append(client).append("\")")
				.append("|> filter(fn: (r) => r[\"big_name\"] == \"").append(sensorType).append("\")")
				.append("|> group(columns: [\"name\"])")
				.append("|> last()")
				.append("|> map(fn: (r) => ({value:r._value,name:r.name}))");
		String query = queryBuilder.toString();
		queryBuilder.setLength(0);

		List<FluxTable> tables = influxDBClient.getQueryApi().query(query, "semse");
		List<Map<String, Object>> recordsList = new ArrayList<>();
		Map<String, Object> recordMap = new HashMap<>();
		for (FluxTable table : tables) {
			for (FluxRecord record : table.getRecords()) {
				Map<String, Object> valuesMap = record.getValues();
				recordMap.put(valuesMap.get("name").toString(), valuesMap.get("value"));
			}
			recordsList.add(recordMap);
			recordMap = new HashMap<>();
		}
		outMap.put(metric.toString(), recordsList);
	}

	public void executeAndExtractMinMaxData(String client, DataType sensorType, String startTime, DataType outMapKey,
											InfluxDBClient influxDBClient, Map<String, Object> outMap) {
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("max_values = from(bucket: \"week\")")
				.append("|> range(start: -").append(startTime).append(", stop: now())")
				.append("|> filter(fn: (r) => r[\"_measurement\"] == \"").append(client).append("\")")
				.append("|> filter(fn: (r) => r[\"big_name\"] == \"").append(sensorType).append("\")")
				.append("|> group(columns: [\"generate_time\"])")
				.append("|> max(column: \"_value\")")
				.append("|> rename(columns: {_value: \"max_value\"})")

				.append("min_values = from(bucket: \"week\")")
				.append("|> range(start: -").append(startTime).append(", stop: now())")
				.append("|> filter(fn: (r) => r[\"_measurement\"] == \"").append(client).append("\")")
				.append("|> filter(fn: (r) => r[\"big_name\"] == \"").append(sensorType).append("\")")
				.append("|> group(columns: [\"generate_time\"])")
				.append("|> min(column: \"_value\")")
				.append("|> rename(columns: {_value: \"min_value\"})")

				.append("join(tables: {max: max_values, min: min_values},on: [\"generate_time\"])")
				.append("|> map(fn: (r) => ({time: r.generate_time,max_value: r.max_value,min_value: r.min_value}))")
				.append("|> limit(n: 10)");
		String query = queryBuilder.toString();
		queryBuilder.setLength(0);

		List<FluxTable> tables = influxDBClient.getQueryApi().query(query, "semse");

		List<Map<String, Object>> recordsList = new ArrayList<>();
		for (FluxTable table : tables) {
			for (FluxRecord record : table.getRecords()) {
				Map<String, Object> recordMap = new HashMap<>();
				Map<String, Object> valuesMap = record.getValues();
				recordMap.put("time", valuesMap.get("time"));
				recordMap.put("max_value", valuesMap.get("max_value"));
				recordMap.put("min_value", valuesMap.get("min_value"));
				recordsList.add(recordMap);
			}
		}
		outMap.put(outMapKey.toString(), recordsList);
	}

	private String buildQuery(String client, DataType bigName, String startTime) {
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("from(bucket: \"week\")")
				.append("|> range(start: -").append(startTime).append(", stop: now())")
				.append("|> filter(fn: (r) => r[\"_measurement\"] == \"").append(client).append("\")")
				.append("|> filter(fn: (r) => r[\"big_name\"] == \"").append(bigName).append("\")")
				.append("|> map(fn: (r) => ({value:r._value,time:r.generate_time,name:r.name})) ")
				.append("|> limit(n:10)");
		String query = queryBuilder.toString();
		log.debug(query);

		queryBuilder.setLength(0);
		return query;
	}



}
