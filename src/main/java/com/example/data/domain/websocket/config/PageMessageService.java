package com.example.data.domain.websocket.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Getter @Setter
public class PageMessageService {

    private final ObjectMapper objectMapper;
    private String connectionMessage = "1"; // 'final' 키워드 제거
    private final InfluxDBClient influxDBClient;

    public PageMessageService(ObjectMapper objectMapper, InfluxDBClient influxDBClient) {
        this.objectMapper = objectMapper;
        this.influxDBClient = influxDBClient;
    }

    public String machineState() throws Exception {
        String client = "CLIENT" + this.connectionMessage;
        String query = "from(bucket: \"day\")" +
                "  |> range(start: -2m, stop: now())" +
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
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String machineSensor() throws Exception {
        String client = "CLIENT" + this.connectionMessage;
        // velocity
        List<Map<String, Object>> outList = new ArrayList<>();
        Map<String, Object> outMap = new HashMap<>();
        String query = "from(bucket: \"week\")" +
                "|> range(start: -5m, stop: now())" +
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
                "|> range(start: -5m, stop: now())" +
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
                "|> range(start: -2d, stop: now())" +
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
                "|> range(start: -1m, stop: now())" +
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
                "|> range(start: -10m, stop: now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"AIR_OUT_MPA\")" +
                "|> group(columns: [\"generate_time\"])" +
                "|> max(column: \"_value\")" +
                "|> rename(columns: {_value: \"max_value\"})" +

                "min_values = from(bucket: \"week\")" +
                "|> range(start: -10m, stop: now())" +
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
                "|> range(start: -30m, stop: now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"AIR_OUT_KPA\")" +
                "|> group(columns: [\"generate_time\"])" +
                "|> max(column: \"_value\")" +
                "|> rename(columns: {_value: \"max_value\"})" +

                "min_values = from(bucket: \"week\")" +
                "|> range(start: -30m, stop: now())" +
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
                "|> range(start: -5m, stop: now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"VACUUM\")" +
                "|> group(columns: [\"generate_time\"])" +
                "|> max(column: \"_value\")" +
                "|> rename(columns: {_value: \"max_value\"})" +

                "min_values = from(bucket: \"week\")" +
                "|> range(start: -5m, stop: now())" +
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
                "|> range(start: -5m, stop: now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"MOTOR\")" +
                "|> group(columns: [\"generate_time\"])" +
                "|> max(column: \"_value\")" +
                "|> rename(columns: {_value: \"max_value\"})" +

                "min_values = from(bucket: \"week\")" +
                "|> range(start: -5m, stop: now())" +
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
                "|> range(start: -1m, stop: now())" +
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

    public String machineMotor() throws Exception {
        String client = "CLIENT" + this.connectionMessage;
        String query = "from(bucket: \"week\")" +
                "  |> range(start: -2m, stop: now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"MOTOR\")" +
                "  |> group(columns:[\"name\"]) " +
                "  |> map(fn: (r) => ({value:r._value,time:r.generate_time,name:r.name})) " +
                "  |> limit(n:10)";
        return queryClientToJson(query);
    }

    public String machineAirInKpa() throws Exception {
        String client = "CLIENT" + this.connectionMessage;

        String query = "from(bucket: \"week\")" +
                "  |> range(start: -24s, stop:now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"AIR_IN_KPA\")" +
                "  |> group(columns:[\"name\"]) " +
                "  |> map(fn: (r) => ({value:r._value,time:r.generate_time,name:r.name})) " +
                "  |> limit(n:10)";
        return queryClientToJson(query);
    }

    public String machinAirOutKpa() throws Exception {
        String client = "CLIENT" + this.connectionMessage;

        String query = "from(bucket: \"week\")" +
                "  |> range(start: -10m, stop:now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"AIR_OUT_KPA\")" +
                "  |> group(columns:[\"name\"]) " +
                "  |> map(fn: (r) => ({value:r._value,time:r.generate_time,name:r.name})) " +
                "  |> limit(n:10)";
        return queryClientToJson(query);
    }

    public String machineAirOutMpa() throws Exception {
        String client = "CLIENT" + this.connectionMessage;

        String query = "from(bucket: \"week\")" +
                "  |> range(start: -4m, stop:now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"AIR_OUT_MPA\")" +
                "  |> group(columns:[\"name\"]) " +
                "  |> map(fn: (r) => ({value:r._value,time:r.generate_time,name:r.name})) " +
                "  |> limit(n:10)";
        return queryClientToJson(query);
    }

    public String machineVacuum() throws Exception {
        String client = "CLIENT" + this.connectionMessage;

        String query = "from(bucket: \"week\")" +
                "  |> range(start: -4m, stop:now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"VACUUM\")" +
                "  |> group(columns:[\"name\"]) " +
                "  |> map(fn: (r) => ({value:r._value,time:r.generate_time,name:r.name})) " +
                "  |> limit(n:10)";
        return queryClientToJson(query);
    }

    public String machineWater() throws Exception {
        String client = "CLIENT" + this.connectionMessage;

        String query = "from(bucket: \"week\")" +
                "  |> range(start: -40s, stop:now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"WATER\")" +
                "  |> group(columns:[\"name\"]) " +
                "  |> map(fn: (r) => ({value:r._value,time:r.generate_time,name:r.name})) " +
                "  |> limit(n:10)";
        return queryClientToJson(query);
    }

    public String machineAbrasion() throws Exception {
        String client = "CLIENT" + this.connectionMessage;

        String query = "from(bucket: \"week\")" +
                "  |> range(start: -2d, stop:now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"ABRASION\")" +
                "  |> group(columns:[\"name\"]) " +
                "  |> map(fn: (r) => ({value:r._value,time:r.generate_time,name:r.name})) " +
                "  |> limit(n:10)";
        return queryClientToJson(query);
    }

    public String machineLoad() throws Exception {
        String client = "CLIENT" + this.connectionMessage;

        String query = "from(bucket: \"week\")" +
                "  |> range(start: -24m, stop:now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"LOAD\")" +
                "  |> group(columns:[\"name\"]) " +
                "  |> map(fn: (r) => ({value:r._value,time:r.generate_time,name:r.name})) " +
                "  |> limit(n:10)";
        return queryClientToJson(query);
    }

    public String machineVelocity() throws Exception {
        String client = "CLIENT" + this.connectionMessage;

        String query = "from(bucket: \"week\")" +
                "  |> range(start: -24m, stop:now())" +
                "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                "|> filter(fn: (r) => r[\"big_name\"] == \"VELOCITY\")" +
                "  |> group(columns:[\"name\"]) " +
                "  |> map(fn: (r) => ({value:r._value,time:r.generate_time,name:r.name})) " +
                "  |> limit(n:10)";
        return queryClientToJson(query);
    }

    public String mainMachine() throws Exception {
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

    // 함수
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

    private Object timeToSecond(Object timestamp) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd/HH:mm:ss.SSS");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd/HH:mm:ss");
        return LocalDateTime.parse((CharSequence) timestamp, inputFormatter).format(outputFormatter);
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
                        "  |> range(start: -2m)" +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \""+ client +"\")" +
                        "|> filter(fn: (r) => r[\"big_name\"] == \""+ sensor +"\")" +
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

    public String mainTest() {
        String query = "from(bucket: \"week\")" +
                "|> group(columns: [\"_measurement\", \"big_name\"])" +
                "|> aggregateWindow(every: 2m, fn: mean, createEmpty: false)" +
                "|> pivot(rowKey:[\"_time\"], columnKey: [\"_measurement\", \"big_name\"], valueColumn: \"_value\")" +
                "|> last()";
        List<FluxTable> tables = influxDBClient.getQueryApi().query(query, "semse");
        Map<String, Map<String, Double>> result = new HashMap<>();

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                String client = record.getMeasurement();
                String bigName = Objects.requireNonNull(record.getValueByKey("big_name")).toString();
                Map<String, Double> clientData = result.getOrDefault(client, new HashMap<>());
                clientData.put(bigName, (Double) record.getValue());
                result.put(client, clientData);
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
