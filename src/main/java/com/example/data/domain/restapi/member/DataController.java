//package com.example.data.domain.restapi.member;
//
//import com.example.data.entity.global.Data;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/clients")
//public class DataController {
//    @Autowired
//    private DataService dataService;
//
//    @PostMapping
//    public Data saveData(@RequestBody Data data) {
//        return dataService.saveClient(data);
//    }
//
//    @GetMapping("/{id}")
//    public Data getDataById(@PathVariable Long id) {
//        return dataService.getClientById(id);
//    }
//
//}
