package com.example.data.domain.websocket.config;

import org.springframework.messaging.handler.annotation.MessageMapping;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@AllArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final PageMessageService pageMessageService;

    @MessageMapping("/machine/state")
    public void machineState(String number) throws Exception {
        pageMessageService.setConnectionMessage(number);
        String responseMessage = pageMessageService.machineState();
        messagingTemplate.convertAndSend("/client/machine/state", responseMessage);
    }

    @MessageMapping("/machine/sensor")
    public void machineSensor(String number) throws Exception {
        pageMessageService.setConnectionMessage(number);
        String responseMessage = pageMessageService.machineSensor();
        messagingTemplate.convertAndSend("/client/machine/sensor", responseMessage);
    }

    @MessageMapping("/machine/motor")
    public void machineMotor(String number) throws Exception {
        pageMessageService.setConnectionMessage(number);
        String responseMessage = pageMessageService.machineMotor();
        messagingTemplate.convertAndSend("/client/machine/motor", responseMessage);
    }

    @MessageMapping("/machine/air_in_kpa")
    public void machineAirInKpa(String number) throws Exception {
        pageMessageService.setConnectionMessage(number);
        String responseMessage = pageMessageService.machineAirInKpa();
        messagingTemplate.convertAndSend("/client/machine/air_in_kpa", responseMessage);
    }

    @MessageMapping("/machine/air_out_kpa")
    public void machineAirOutKpa(String number) throws Exception {
        pageMessageService.setConnectionMessage(number);
        String responseMessage = pageMessageService.machinAirOutKpa();
        messagingTemplate.convertAndSend("/client/machine/air_out_kpa", responseMessage);
    }

    @MessageMapping("/machine/air_out_mpa")
    public void machineAirInMpa(String number) throws Exception {
        pageMessageService.setConnectionMessage(number);
        String responseMessage = pageMessageService.machineAirOutMpa();
        messagingTemplate.convertAndSend("/client/machine/air_out_mpa", responseMessage);
    }

    @MessageMapping("/machine/vacuum")
    public void machineVacuum(String number) throws Exception {
        pageMessageService.setConnectionMessage(number);
        String responseMessage = pageMessageService.machineVacuum();
        messagingTemplate.convertAndSend("/client/machine/vacuum", responseMessage);
    }

    @MessageMapping("machine/water")
    public void machineWater(String number) throws Exception {
        pageMessageService.setConnectionMessage(number);
        String responseMessage = pageMessageService.machineWater();
        messagingTemplate.convertAndSend("/client/machine/water", responseMessage);
    }

    @MessageMapping("/machine/abrasion")
    public void machineAbrasion(String number) throws Exception {
        pageMessageService.setConnectionMessage(number);
        String responseMessage = pageMessageService.machineAbrasion();
        messagingTemplate.convertAndSend("/client/machine/abrasion", responseMessage);
    }

    @MessageMapping("/machine/load")
    public void machineLoad(String number) throws Exception {
        pageMessageService.setConnectionMessage(number);
        String responseMessage = pageMessageService.machineLoad();
        messagingTemplate.convertAndSend("/client/machine/load", responseMessage);
    }

    @MessageMapping("/machine/velocity")
    public void machineVelocity(String number) throws Exception {
        pageMessageService.setConnectionMessage(number);
        String responseMessage = pageMessageService.machineVelocity();
        messagingTemplate.convertAndSend("/client/machine/velocity", responseMessage);
    }

    @MessageMapping("/main/machine")
    public void mainMachine(String number) throws Exception {
        String faNumber = "1";
        pageMessageService.setConnectionMessage(faNumber);
        String responseMessage = pageMessageService.mainMachine();
        messagingTemplate.convertAndSend("/client/main/machine", responseMessage);
    }


}
