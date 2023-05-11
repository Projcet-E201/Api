//package com.example.data.domain.restapi.member;
//
//
//import com.example.data.entity.global.Data;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import javax.persistence.EntityNotFoundException;
//
//@Service
//public class DataService {
//    @Autowired
//    private DataRepository dataRepository;
//
//    public Data saveClient(Data client) {
//        return dataRepository.save(client);
//    }
//
//    public Data getClientById(Long id) {
//        return dataRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + id));
//    }
//
//    public Data getClientByName(String name) {
//        return dataRepository.findByName(name).orElseThrow(() -> new EntityNotFoundException("Client not found with name: " + name));
//    }

//}
