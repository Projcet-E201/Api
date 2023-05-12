package com.example.data.domain.websocket.config;

import com.example.data.util.constants.TimeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RestController
public class DataWebSocketController {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private InfluxDBClient influxDBClient;

    @MessageMapping("/machine/sensor")
    @SendTo("/client/machine/sensor")
    public String machineSensor(@RequestBody String data) throws Exception {
        String client = "CLIENT" + data;
        // velocity
        List<Map<String, Object>> outList = new ArrayList<>();
        Map<String, Object> outMap = new HashMap<>();
        String query = "from(bucket: \"week\")" +
                "|> range(start: -"+ TimeInfo.MACHINE_SENSOR_VELOCITY_START + ", stop: now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"VELOCITY\")" +
                "|> group(columns: [\"name\"])" +
                "|> last()" +
                "|> map(fn: (r) => ({value:r._value,name:r.name}))";

        List<FluxTable> tables = influxDBClient.getQueryApi().query(query, "semse");
        List<Map<String, Object>> recordsList = new ArrayList<>();
        Map<String, Object> recordMap = new HashMap<>();
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Map<String, Object> valuesMap = record.getValues();
                recordMap.put(valuesMap.get("name").toString(), valuesMap.get("value"));
            }
        }
        recordsList.add(recordMap);
        outMap.put("VELOCITY",recordsList);
        // Load
        query = "from(bucket: \"week\")" +
                "|> range(start: -" + TimeInfo.MACHINE_SENSOR_LOAD_START + ", stop: now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"LOAD\")" +
                "|> group(columns: [\"name\"])" +
                "|> last()" +
                "|> map(fn: (r) => ({value:r._value,name:r.name}))";

        tables = influxDBClient.getQueryApi().query(query, "semse");
        recordsList = new ArrayList<>();
        recordMap = new HashMap<>();
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Map<String, Object> valuesMap = record.getValues();
                recordMap.put(valuesMap.get("name").toString(), valuesMap.get("value"));
            }
        }
        recordsList.add(recordMap);
        outMap.put("LOAD",recordsList);
        //ABRASION
        query = "from(bucket: \"week\")" +
                "|> range(start: -" + TimeInfo.MACHINE_SENSOR_ABRASION_START + ", stop: now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"ABRASION\")" +
                "|> group(columns: [\"name\"])" +
                "|> last()" +
                "|> map(fn: (r) => ({value:r._value,name:r.name}))";

        tables = influxDBClient.getQueryApi().query(query, "semse");
        recordsList = new ArrayList<>();
        recordMap = new HashMap<>();
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Map<String, Object> valuesMap = record.getValues();
                recordMap.put(valuesMap.get("name").toString(), valuesMap.get("value"));
            }
        }
        recordsList.add(recordMap);
        outMap.put("ABRASION",recordsList);
        //WATER
        query = "from(bucket: \"week\")" +
                "|> range(start: -" + TimeInfo.MACHINE_SENSOR_WATER_START + ", stop: now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"WATER\")" +
                "|> group(columns: [\"name\"])" +
                "|> last()" +
                "|> map(fn: (r) => ({value:r._value,name:r.name}))";

        tables = influxDBClient.getQueryApi().query(query, "semse");
        recordsList = new ArrayList<>();
        recordMap = new HashMap<>();
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Map<String, Object> valuesMap = record.getValues();
                recordMap.put(valuesMap.get("name").toString(), valuesMap.get("value"));
            }
        }
        recordsList.add(recordMap);
        outMap.put("WATER",recordsList);
        //AIR_OUT_KPA
        query = "max_values = from(bucket: \"week\")" +
                "|> range(start: -" + TimeInfo.MACHINE_SENSOR_AIR_OUT_MPA_START + ", stop: now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"AIR_OUT_MPA\")" +
                "|> group(columns: [\"generate_time\"])" +
                "|> max(column: \"_value\")" +
                "|> rename(columns: {_value: \"max_value\"})" +

                "min_values = from(bucket: \"week\")" +
                "|> range(start: -" + TimeInfo.MACHINE_SENSOR_AIR_OUT_MPA_START + ", stop: now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \"AIR_OUT_MPA\")" +
                "|> group(columns: [\"generate_time\"])" +
                "|> min(column: \"_value\")" +
                "|> rename(columns: {_value: \"min_value\"})" +

                "join(tables: {max: max_values, min: min_values},on: [\"generate_time\"])" +
                "|> map(fn: (r) => ({time: r.generate_time,max_value: r.max_value,min_value: r.min_value}))" +
                "|> limit(n: 10)";
        tables = influxDBClient.getQueryApi().query(query, "semse");
        recordsList = new ArrayList<>();
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                recordMap = new HashMap<>();
                Map<String, Object> valuesMap = record.getValues();
                recordMap.put("time", valuesMap.get("time"));
                recordMap.put("max_value", valuesMap.get("max_value"));
                recordMap.put("min_value", valuesMap.get("min_value"));
                recordsList.add(recordMap);
//                System.out.println("recordMap = " + recordMap);
            }
        }
        outMap.put("AIR_OUT_MPA",recordsList);
        //AIR_OUT_MPA
        query = "max_values = from(bucket: \"week\")" +
                "|> range(start: -" + TimeInfo.MACHINE_SENSOR_AIR_OUT_KPA_START + ", stop: now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"AIR_OUT_KPA\")" +
                "|> group(columns: [\"generate_time\"])" +
                "|> max(column: \"_value\")" +
                "|> rename(columns: {_value: \"max_value\"})" +

                "min_values = from(bucket: \"week\")" +
                "|> range(start: -" + TimeInfo.MACHINE_SENSOR_AIR_OUT_KPA_START + ", stop: now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"AIR_OUT_KPA\")" +
                "|> group(columns: [\"generate_time\"])" +
                "|> min(column: \"_value\")" +
                "|> rename(columns: {_value: \"min_value\"})" +

                "join(tables: {max: max_values, min: min_values},on: [\"generate_time\"])" +
                "|> map(fn: (r) => ({time: r.generate_time,max_value: r.max_value,min_value: r.min_value}))" +
                "|> limit(n: 10)";
        tables = influxDBClient.getQueryApi().query(query, "semse");
        recordsList = new ArrayList<>();
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                recordMap = new HashMap<>();
                Map<String, Object> valuesMap = record.getValues();
                recordMap.put("time", valuesMap.get("time"));
                recordMap.put("max_value", valuesMap.get("max_value"));
                recordMap.put("min_value", valuesMap.get("min_value"));
                recordsList.add(recordMap);
//                System.out.println("recordMap = " + recordMap);
            }
        }
        outMap.put("AIR_OUT_KPA",recordsList);
        //VACUUM
        query = "max_values = from(bucket: \"week\")" +
                "|> range(start: -" + TimeInfo.MACHINE_SENSOR_VACUUM_START + ", stop: now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"VACUUM\")" +
                "|> group(columns: [\"generate_time\"])" +
                "|> max(column: \"_value\")" +
                "|> rename(columns: {_value: \"max_value\"})" +

                "min_values = from(bucket: \"week\")" +
                "|> range(start: -" + TimeInfo.MACHINE_SENSOR_VACUUM_START + ", stop: now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"VACUUM\")" +
                "|> group(columns: [\"generate_time\"])" +
                "|> min(column: \"_value\")" +
                "|> rename(columns: {_value: \"min_value\"})" +

                "join(tables: {max: max_values, min: min_values},on: [\"generate_time\"])" +
                "|> map(fn: (r) => ({time: r.generate_time,max_value: r.max_value,min_value: r.min_value}))" +
                "|> limit(n: 10)";
        tables = influxDBClient.getQueryApi().query(query, "semse");
        recordsList = new ArrayList<>();
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                recordMap = new HashMap<>();
                Map<String, Object> valuesMap = record.getValues();
                recordMap.put("time", valuesMap.get("time"));
                recordMap.put("max_value", valuesMap.get("max_value"));
                recordMap.put("min_value", valuesMap.get("min_value"));
                recordsList.add(recordMap);
//                System.out.println("recordMap = " + recordMap);
            }
        }
        outMap.put("VACUUM",recordsList);
        //MOTOR
        query = "max_values = from(bucket: \"week\")" +
                "|> range(start: -" + TimeInfo.MACHINE_SENSOR_MOTOR_START + ", stop: now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"MOTOR\")" +
                "|> group(columns: [\"generate_time\"])" +
                "|> max(column: \"_value\")" +
                "|> rename(columns: {_value: \"max_value\"})" +

                "min_values = from(bucket: \"week\")" +
                "|> range(start: -" + TimeInfo.MACHINE_SENSOR_MOTOR_START + ", stop: now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"MOTOR\")" +
                "|> group(columns: [\"generate_time\"])" +
                "|> min(column: \"_value\")" +
                "|> rename(columns: {_value: \"min_value\"})" +

                "join(tables: {max: max_values, min: min_values},on: [\"generate_time\"])" +
                "|> map(fn: (r) => ({time: r.generate_time,max_value: r.max_value,min_value: r.min_value}))" +
                "|> limit(n: 10)";
        tables = influxDBClient.getQueryApi().query(query, "semse");
        recordsList = new ArrayList<>();
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                recordMap = new HashMap<>();
                Map<String, Object> valuesMap = record.getValues();
                recordMap.put("time", valuesMap.get("time"));
                recordMap.put("max_value", valuesMap.get("max_value"));
                recordMap.put("min_value", valuesMap.get("min_value"));
                recordsList.add(recordMap);
//                System.out.println("recordMap = " + recordMap);
            }
        }
        outMap.put("MOTOR",recordsList);
        // air_in_kpa
        query = "from(bucket: \"week\")" +
                "|> range(start: -" + TimeInfo.MACHINE_SENSOR_AIR_IN_KPA_START + ", stop: now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"AIR_IN_KPA\")" +
                "|> group(columns: [\"generate_time\"])" +
                "|> mean(column: \"_value\")" +
                "|> map(fn: (r) => ({value:r._value,time:r.generate_time }))" +
                "|> limit(n:10)";
        tables = influxDBClient.getQueryApi().query(query, "semse");
        recordsList = new ArrayList<>();
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
        outMap.put("AIR_IN_KPA",recordsList);
        // 마무리
        outList.add(outMap);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(outList);
    }

    @MessageMapping("/machine/state")
    @SendTo("/client/machine/state")
    public String machineState(@RequestBody String data) {
        String client = "CLIENT" + data;
        String query = "from(bucket: \"day\")" +
                "  |> range(start: -"+ TimeInfo.MACHINE_STATE_START +", stop: now())" +
                "  |> filter(fn: (r) => r[\"_measurement\"] == \"" + client +"\")" +
                "  |> group(columns:[\"name\"]) " +
                "  |> last()" +
                "  |> map(fn: (r) => ({value:r._value,time:r.generate_time,name:r.name}))";
        List<FluxTable> tables = influxDBClient.getQueryApi().query(query, "semse");
        List<Map<String, Object>> recordsList = new ArrayList<>();
        Map<String, Object> recordMap = null;
        String currentPrefix = null;

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Map<String, Object> valuesMap = record.getValues();
                String name = valuesMap.get("name").toString();

                // Name의 접두어를 가져옵니다.
                String prefix = name.replaceAll("([a-zA-Z]*).*", "$1");

                // Name의 접두어가 변경되었거나 recordMap이 아직 생성되지 않은 경우
                if (recordMap == null || !prefix.equals(currentPrefix)) {
                    currentPrefix = prefix;

                    // 이전 recordMap에 대해 누락된 키를 추가합니다.
                    if (recordMap != null) {
                        for (int i = 1; i <= 10; i++) {
                            String key = currentPrefix + i;
                            recordMap.putIfAbsent(key, null);
                        }
                    }

                    recordMap = new HashMap<>();
                    recordsList.add(recordMap);  // 생성된 딕셔너리를 리스트에 추가
                }
                if (currentPrefix.equals("string")) {
                    Map<String, Object> innerValueMap = new HashMap<>();
                    innerValueMap.put("time", valuesMap.get("time"));
                    innerValueMap.put("value", valuesMap.get("value"));
                    recordMap.put(name, innerValueMap);
                } else {
                    recordMap.put(name, valuesMap.get("value"));
                }
            }
        }
        // 마지막 recordMap에 대해 누락된 키를 추가합니다.
        if (recordMap != null) {
            for (int i = 1; i <= 10; i++) {
                String key = currentPrefix + i;
                recordMap.putIfAbsent(key, null);
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(recordsList);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @MessageMapping("/machine/motor")
    @SendTo("/client/machine/motor")
    public String machineMotor(@RequestBody String data) throws Exception {
        String client = "CLIENT" + data;
        String query = "from(bucket: \"week\")" +
                "  |> range(start: -"+ TimeInfo.MACHINE_MOTOR_START + ", stop: now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"MOTOR\")" +
                "  |> group(columns:[\"name\"]) " +
                "  |> map(fn: (r) => ({value:r._value,time:r.generate_time,name:r.name})) " +
                "  |> limit(n:10)";
        return queryClientToJson(query);
    }


    @MessageMapping("/machine/air_in_kpa")
    @SendTo("/client/machine/air_in_kpa")
    public String machinAirInKpa(@RequestBody String data) throws Exception {
        String client = "CLIENT" + data;

        String query = "from(bucket: \"week\")" +
                "  |> range(start: -"+ TimeInfo.MACHINE_AIR_IN_KPA_START + ", stop:now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"AIR_IN_KPA\")" +
                "  |> group(columns:[\"name\"]) " +
                "  |> map(fn: (r) => ({value:r._value,time:r.generate_time,name:r.name})) " +
                "  |> limit(n:10)";
        return queryClientToJson(query);
    }

    @MessageMapping("/machine/air_out_kpa")
    @SendTo("/client/machine/air_out_kpa")
    public String machinAirOutKpa(@RequestBody String data) throws Exception {
        String client = "CLIENT" + data;

        String query = "from(bucket: \"week\")" +
                "  |> range(start: -"+ TimeInfo.MACHINE_AIR_OUT_KPA_START + ", stop:now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"AIR_OUT_KPA\")" +
                "  |> group(columns:[\"name\"]) " +
                "  |> map(fn: (r) => ({value:r._value,time:r.generate_time,name:r.name})) " +
                "  |> limit(n:10)";
        return queryClientToJson(query);
    }

    @MessageMapping("/machine/air_out_mpa")
    @SendTo("/client/machine/air_out_mpa")
    public String machinAirOutMpa(@RequestBody String data) throws Exception {
        String client = "CLIENT" + data;

        String query = "from(bucket: \"week\")" +
                "  |> range(start: -"+ TimeInfo.MACHINE_AIR_OUT_MPA_START + ", stop:now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"AIR_OUT_MPA\")" +
                "  |> group(columns:[\"name\"]) " +
                "  |> map(fn: (r) => ({value:r._value,time:r.generate_time,name:r.name})) " +
                "  |> limit(n:10)";
        return queryClientToJson(query);
    }

    @MessageMapping("/machine/vacuum")
    @SendTo("/client/machine/vacuum")
    public String machinVacuum(@RequestBody String data) throws Exception {
        String client = "CLIENT" + data;

        String query = "from(bucket: \"week\")" +
                "  |> range(start: -"+ TimeInfo.MACHINE_VACUUM_START + ", stop:now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"VACUUM\")" +
                "  |> group(columns:[\"name\"]) " +
                "  |> map(fn: (r) => ({value:r._value,time:r.generate_time,name:r.name})) " +
                "  |> limit(n:10)";
        return queryClientToJson(query);
    }

    @MessageMapping("/machine/water")
    @SendTo("/client/machine/water")
    public String machinWater(@RequestBody String data) throws Exception {
        String client = "CLIENT" + data;

        String query = "from(bucket: \"week\")" +
                "  |> range(start: -"+ TimeInfo.MACHINE_WATER_START + ", stop:now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"WATER\")" +
                "  |> group(columns:[\"name\"]) " +
                "  |> map(fn: (r) => ({value:r._value,time:r.generate_time,name:r.name})) " +
                "  |> limit(n:10)";
        return queryClientToJson(query);
    }

    @MessageMapping("/machine/abrasion")
    @SendTo("/client/machine/abrasion")
    public String machinAbrasion(@RequestBody String data) throws Exception {
        String client = "CLIENT" + data;

        String query = "from(bucket: \"week\")" +
                "  |> range(start: -"+ TimeInfo.MACHINE_ABRASION_START + ", stop:now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"ABRASION\")" +
                "  |> group(columns:[\"name\"]) " +
                "  |> map(fn: (r) => ({value:r._value,time:r.generate_time,name:r.name})) " +
                "  |> limit(n:10)";
        return queryClientToJson(query);
    }
    @MessageMapping("/machine/load")
    @SendTo("/client/machine/load")
    public String machinLoad(@RequestBody String data) throws Exception {
        String client = "CLIENT" + data;

        String query = "from(bucket: \"week\")" +
                "  |> range(start: -"+ TimeInfo.MACHINE_LOAD_START + ", stop:now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"LOAD\")" +
                "  |> group(columns:[\"name\"]) " +
                "  |> map(fn: (r) => ({value:r._value,time:r.generate_time,name:r.name})) " +
                "  |> limit(n:10)";
        return queryClientToJson(query);
    }

    @MessageMapping("/machine/velocity")
    @SendTo("/client/machine/velocity")
    public String machinVelocity(@RequestBody String data) throws Exception {
        String client = "CLIENT" + data;

        String query = "from(bucket: \"week\")" +
                "  |> range(start: -"+ TimeInfo.MACHINE_VELOCITY_START + ", stop:now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"VELOCITY\")" +
                "  |> group(columns:[\"name\"]) " +
                "  |> map(fn: (r) => ({value:r._value,time:r.generate_time,name:r.name})) " +
                "  |> limit(n:10)";
        return queryClientToJson(query);
    }
    @MessageMapping("/main/machine")
    @SendTo("/client/main/machine")
    public String MainMachine(@RequestBody String data) throws Exception {
        // 기기 각각의 최신값의 평균을 구하는 코드
        System.out.println("start data = " + data);
        Map<String, Object> outMap = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> sensors = Arrays.asList("MOTOR", "AIR_IN_KPA", "AIR_OUT_KPA", "AIR_OUT_MPA", "LOAD", "VACUUM", "VELOCITY", "WATER");

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
                int timeComparison = ((Comparable) o1.get("time")).compareTo(o2.get("time"));
                if (timeComparison != 0) {
                    return timeComparison;
                }
                // 시간이 같은 경우 이름으로 정렬
                String name1 = (String) o1.get("name");
                String name2 = (String) o2.get("name");
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
            Map<String, Object> sensorAverages = new HashMap<>();
            for (String sensor : sensors) {
                String query = "from(bucket: \"week\")" +
                        "  |> range(start: -"+ TimeInfo.MAIN_MACHINE_START + ")" +
                        "  |> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                        "  |> filter(fn: (r) => r[\"big_name\"] == \""+ sensor +"\")" +
                        "  |> last()" +
                        "  |> group(columns: [\"name\"])" +
                        "  |> last()";
                List<FluxTable> tables = influxDBClient.getQueryApi().query(query, "semse");

                if (!tables.isEmpty()) {
                    FluxRecord record = tables.get(0).getRecords().get(0);
                    if (record != null) {
                        double value = Double.parseDouble(record.getValueByKey("_value").toString());
                        sensorAverages.put(sensor, value);
                    } else {
                        System.out.println("No data for client: " + client + " sensor: " + sensor);
                    }
                }
            }
            return sensorAverages;
        });
    }
//    @MessageMapping("/machine/history")
//    @SendTo("/client/machine/history")
//    public String machineHistory(String data) throws Exception {
//        return null;
//    }
}
