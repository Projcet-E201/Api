package com.example.data.domain.websocket.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class DataWebSocketController {

    @Autowired
    private InfluxDBClient influxDBClient;

    @MessageMapping("/machine/sensor")
    @SendTo("/client/machine/sensor")
    public String testcase(@RequestBody String data) throws Exception {
        String client = "CLIENT" + data;
        System.out.println("client = " + client);
//        List<String> sensors = Arrays.asList("MOTOR","AIR_IN_KPA","AIR_OUT_KPA","AIR_OUT_MPA","LOAD","VACUUM","VELOCITY","WATER");
        String query = "from(bucket: \""+ client + "\") |> range(start: -1m)" +
                " |> filter(fn: (r) => r[\"_measurement\"] == \"MOTOR\")";
        String json = queryClientToJson(query);
        return json;
    }
// 성공 케이스
//    @MessageMapping("/post")
//    @SendTo("/client/get")
//    public String testcase(@RequestBody String data) throws Exception {
//        String query = "from(bucket: \"CLIENT1\") |> range(start: -1m)" +
//                " |> filter(fn: (r) => r[\"_measurement\"] == \"MOTOR\")";
//        String json = queryClientToJson(query);
//        return json;
//    }
//    @MessageMapping("/main/machine")
//    @SendTo("/client/main/machine")
//    public String MainMachine(@RequestBody String data) throws Exception {
//        List<Map<String, Object>> mainList = new ArrayList<>;
//        ObjectMapper mainMapper = new ObjectMapper();
//        for (int i = 1; i < 7; i++) {
//            ObjectMapper valueMapper = new ObjectMapper();
//            String client = "CLIENT" + String.valueOf(i);
//            // 센서 종류
//            List<String> sensors = Arrays.asList("MOTOR","AIR_IN_KPA","AIR_OUT_KPA","AIR_OUT_MPA","LOAD","VACUUM","VELOCITY","WATER");
//
//        }
//    }


    @MessageMapping("/main/machine")
    @SendTo("/client/main/machine")
    public String MainMachine(@RequestBody String data) throws Exception {
        // 기기 각각의 최신값의 평균을 구하는 코드
        List<Map<String, Object>> out_list = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        for (int i = 1; i < 7; i++) {
            String client = "CLIENT" + String.valueOf(i);
            Map<String, Object> out_dic = new HashMap<>();
            out_dic.put("name", client);
            Date now = new Date();
            String nowTime = now.toString();
            out_dic.put("time", nowTime);
            List<String> sensors = Arrays.asList("MOTOR","AIR_IN_KPA","AIR_OUT_KPA","AIR_OUT_MPA","LOAD","VACUUM","VELOCITY","WATER");
            List<Object> new_list = new ArrayList<>();
            for (String sensor : sensors) {
                String query = "from(bucket: \""+ client +"\") |> range(start: -2m)" +
                        " |> filter(fn: (r) => r._measurement == \"" + sensor + "\")" +
                        " |> last()" +
                        " |> group(columns: [\"name\"])" +
                        " |> last()";
                List<FluxTable> tables = influxDBClient.getQueryApi().query(query, "semse");
                List<FluxRecord> records = new ArrayList<>();

                if (!tables.isEmpty()) {
                    records = tables.get(0).getRecords();
                }
                Map<String, Object> values = new HashMap<>();
                Double sensor_value = 0D;
                for (FluxRecord record : records) {
                    Double value = Double.parseDouble(record.getValueByKey("_value").toString());
                    sensor_value += value;
                }
                int sensor_count;
                if (sensor == "Motor" | sensor == "AIR_IN_KPA" | sensor == "WATER") {
                    sensor_count = 10;
                } else if (sensor == "VACUUM") {
                    sensor_count = 30;
                } else {
                    sensor_count = 5;
                }
                // 여기서 sensor를
                values.put(sensor, sensor_value/sensor_count);

                new_list.add(values);
            }
            out_dic.put("value", new_list);
            out_list.add(out_dic);
        }
        String json = objectMapper.writeValueAsString(out_list);
        return json;
    }
    private String queryClientToMaxMinJson(String query) throws JsonProcessingException {
        List<FluxTable> tables = influxDBClient.getQueryApi().query(query, "semse");
        List<Map<String, Object>> recordsList = new ArrayList<>();
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Map<String, Object> recordMap = new HashMap<>();
                Map<String, Object> valuesMap = record.getValues();
                recordMap.put("name", valuesMap.get("name"));
                recordMap.put("value", valuesMap.get("_value"));
                Object finalValue = timeToSecond(valuesMap.get("generate_time"));
                recordMap.put("time", finalValue);
                recordsList.add(recordMap);
                System.out.println("recordMap = " + recordMap);
            }

        }
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> maxMinList = new ArrayList<>();
        // name별로 그룹핑하고 최대 최소 구하기
        Map<String, List<Map<String, Object>>> groupByNameMap = recordsList.stream()
                .collect(Collectors.groupingBy(r -> r.get("name").toString()));
        for (String name : groupByNameMap.keySet()) {
            List<Map<String, Object>> groupList = groupByNameMap.get(name);
            Double max = Double.MIN_VALUE;
            Double min = Double.MAX_VALUE;
            for (Map<String, Object> groupItem : groupList) {
                Double value = Double.parseDouble(groupItem.get("value").toString());
                if (value > max) {
                    max = value;
                }
                if (value < min) {
                    min = value;
                }
            }
            Map<String, Object> maxMinMap = new HashMap<>();
            maxMinMap.put("name", name);
            maxMinMap.put("max_value", max);
            maxMinMap.put("min_value", min);
            maxMinList.add(maxMinMap);
        }

        String json = mapper.writeValueAsString(maxMinList);
// System.out.println("json = " + json);
        return json;
    }

    private String queryClientToJson(String query) throws JsonProcessingException {
        List<FluxTable> tables = influxDBClient.getQueryApi().query(query, "semse");
        List<Map<String, Object>> recordsList = new ArrayList<>();
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Map<String, Object> recordMap = new HashMap<>();
                Map<String, Object> valuesMap = record.getValues();
                recordMap.put("name", valuesMap.get("name"));
                recordMap.put("value", valuesMap.get("_value"));
                Object finalValue = timeToSecond(valuesMap.get("generate_time"));
                recordMap.put("time", finalValue);
                recordsList.add(recordMap);
                System.out.println("recordMap = " + recordMap);
            }

        }
        ObjectMapper mapper = new ObjectMapper();
        // Time 순으로 정렬
        Comparator<Map<String, Object>> timeNameComparator = new Comparator<Map<String, Object>>() {
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
        Collections.sort(recordsList, timeNameComparator);

        String json = mapper.writeValueAsString(recordsList);
//        System.out.println("json = " + json);
        return json;
    }

    private Object timeToSecond(Object timestamp) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd/HH:mm:ss.SSS");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd/HH:mm:ss");
        String formattedDateTime = LocalDateTime.parse((CharSequence) timestamp, inputFormatter).format(outputFormatter);
        Object finalValue = formattedDateTime;
        return finalValue;
    }

    private class NameComparator implements Comparator<String> {
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

}
