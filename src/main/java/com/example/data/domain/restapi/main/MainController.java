package com.example.data.domain.restapi.main;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/main")
//public class MainController {
//    private final MainService mainService;

//    @GetMapping("/sensor")
//    public ResponseEntity<String> mainSensor() {
//        List<Map<String, Object>> list = mainSensor();
//        return ResponseEntity.status(HttpStatus.OK).body(list);
//    }
//}
