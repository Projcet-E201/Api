package com.example.data.domain.websocket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
public class DataWebSocketController {

    @Autowired
    private InfluxDBClient influxDBClient;

    @MessageMapping("/post")
    @SendTo("/client/get")
    public String testcaseAll(@RequestBody String data) throws Exception {
        QueryApi queryApi = influxDBClient.getQueryApi();
        String org = "semse";
        String query = "from(bucket: \"three day\") |> range(start: -1m)" +
                " |> filter(fn: (r) => r[\"_measurement\"] == \"SERVER1\")" +
                " |> filter(fn: (r) => r[\"big_name\"] == \"MOTOR\")";
        ;
        List<FluxTable> tables = influxDBClient.getQueryApi().query(query, org);
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
        System.out.println("json = " + json);

        return json;
    }

    private Object timeToSecond(Object timestamp) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd/HH:mm:ss.SSS");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd/HH:mm:ss");
        String formattedDateTime = LocalDateTime.parse((CharSequence) timestamp, inputFormatter).format(outputFormatter);
        Object finalValue = formattedDateTime;
        return finalValue;
    }

    public class NameComparator implements Comparator<String> {
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


    @MessageMapping("/get")
    @SendTo("/post")
    public String test1() {
        String test = "data 보내는중2";
        System.out.println("test = " + test);
        return test;
    }

}
