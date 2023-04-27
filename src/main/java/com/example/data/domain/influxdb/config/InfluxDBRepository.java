package com.example.data.domain.influxdb.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class InfluxDBRepository {

    @Autowired
    private InfluxDBClient influxDBClient;


    public List<FluxTable> getDailyData() {
        String org = "semse";
        String query = "from(bucket: \"day\")" +
                          "|> range(start: v.timeRangeStart, stop: v.timeRangeStop)" +
                          "|> filter(fn: (r) => r[\"_measurement\"] == \"SERVER1\")" +
                          "|> filter(fn: (r) => r[\"_field\"] == \"value\")";
        List<FluxTable> tables = influxDBClient.getQueryApi().query(query, org);
        return tables;
    }
}

