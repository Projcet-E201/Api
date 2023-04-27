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
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
public class DataWebSocketController {

    @Autowired
    private InfluxDBClient influxDBClient;
    @MessageMapping("/post")
    @SendTo("/client/get")
    public String testcaseAll(@RequestBody String data) throws Exception{
        QueryApi queryApi = influxDBClient.getQueryApi();
        String org = "semse";
        String query = "from(bucket: \"day\") |> range(start: -1d)" +
                " |> filter(fn: (r) => r[\"_measurement\"] == \"SERVER1\")";
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

    @MessageMapping("/get")
    @SendTo("/post")
    public String test1() {
        String test = "data 보내는중2";
        System.out.println("test = " + test);
        return test;
    }

}
