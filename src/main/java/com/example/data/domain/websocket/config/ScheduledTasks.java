package com.example.data.domain.websocket.config;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final SimpMessagingTemplate messagingTemplate;
    private final PageMessageService pageMessageService;
    private final WebSocketSessionManager sessionManager;

    @Scheduled(fixedRate = 1000) // 1000ms(1초)마다 메서드를 실행
    public void sendPeriodicMessages() throws Exception {

        String machineState = pageMessageService.machineState();
        messagingTemplate.convertAndSend("/client/machine/state", machineState);

        String machineSensor = pageMessageService.machineSensor();
        messagingTemplate.convertAndSend("/client/machine/sensor", machineSensor);

        String machineMotor = pageMessageService.machineMotor();
        messagingTemplate.convertAndSend("/client/machine/motor", machineMotor);

        String machineAirInKpa = pageMessageService.machineAirInKpa();
        messagingTemplate.convertAndSend("/client/machine/air_in_kpa", machineAirInKpa);

        String machineAirOutKpa = pageMessageService.machinAirOutKpa();
        messagingTemplate.convertAndSend("/client/machine/air_out_kpa", machineAirOutKpa);

        String machineVacuum = pageMessageService.machineVacuum();
        messagingTemplate.convertAndSend("/client/machine/vacuum", machineVacuum);

        String machineWater = pageMessageService.machineWater();
        messagingTemplate.convertAndSend("/client/machine/water", machineWater);

        String machineAbrasion = pageMessageService.machineAbrasion();
        messagingTemplate.convertAndSend("/client/machine/abrasion", machineAbrasion);

        String machineLoad = pageMessageService.machineLoad();
        messagingTemplate.convertAndSend("/client/machine/load", machineLoad);

        String machineVelocity = pageMessageService.machineVelocity();
        messagingTemplate.convertAndSend("/client/machine/velocity", machineVelocity);

    }

    @Scheduled(fixedRate = 10000) // 1000ms(10초)마다 메서드를 실행
    public void sendPeriodicMainMessages() throws Exception {
        String mainMachine = pageMessageService.mainMachine();
        messagingTemplate.convertAndSend("/client/main/machine", mainMachine);
    }

}
