package com.JJP.restapiserver.service.member;

import com.JJP.restapiserver.domain.dto.member.response.AlertResponseDto;
import com.JJP.restapiserver.domain.entity.member.Alert;
import com.JJP.restapiserver.repository.member.AlertRepository;
import com.JJP.restapiserver.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;

    private final MemberRepository memberRepository;
    @Override
    public List<AlertResponseDto> getRecentAlertList(Long member_id) {
        List<Alert> alertList = alertRepository.findTop6ByReceiver_idOrderByCreatedDateDesc(member_id);
        List<AlertResponseDto> alertResponseDtoList = new ArrayList<>();
        if(alertList != null){
            for(Alert alert : alertList){
                alertResponseDtoList.add(AlertResponseDto.builder()
                        .id(alert.getId())
                        .senderId(alert.getSender().getId())
                        .senderName(alert.getSender().getNickname())
                        .receiverId(alert.getReceiver().getId())
                        .receiverName(alert.getReceiver().getNickname())
                        .type(alert.getType())
                        .index(alert.getSequence())
                        .message(alert.getMessage())
                        .check(alert.getIs_read())
                        .createdTime(alert.getCreatedDate())
                        .build());
            }
        }
        return alertResponseDtoList;
    }


    @Override
    public void readAlert(Long alert_id) {
        Alert alert = alertRepository.getById(alert_id);
        alert.read();
    }

    @Override
    public void readAllAlert(Long member_id) {
        List<Alert> alertList = alertRepository.findByReceiver_id(member_id);
        for(Alert alert : alertList){
            if(alert.getIs_read() == 0)
                alert.read();
        }
    }

    @Override
    public List<AlertResponseDto> getAllAlertList(Long member_id) {
        List<Alert> alertList = alertRepository.findByReceiver_id(member_id);
        List<AlertResponseDto> alertResponseDtoList = new ArrayList<>();
        if(alertList != null){
            for(Alert alert : alertList){
                alertResponseDtoList.add(AlertResponseDto.builder()
                        .id(alert.getId())
                        .senderId(alert.getSender().getId())
                        .senderName(alert.getSender().getNickname())
                        .receiverId(alert.getReceiver().getId())
                        .receiverName(alert.getReceiver().getNickname())
                        .type(alert.getType())
                        .index(alert.getSequence())
                        .message(alert.getMessage())
                        .check(alert.getIs_read())
                        .createdTime(alert.getCreatedDate())
                        .build());
            }
        }
        return alertResponseDtoList;
    }

    //
    @Override
    public AlertResponseDto saveAlert(Long senderId, Long receiverId, String type, Long index, String msg) {
        LocalDateTime now = LocalDateTime.now();
        List<Alert> recent_alerts = alertRepository.findDuplicatedMessage(receiverId, "follow",now);
        if(!recent_alerts.isEmpty()){
            return null;
        }
        Alert alert = Alert.builder()
                .sender(memberRepository.getById(senderId))
                .receiver(memberRepository.getById(receiverId))
                .message(msg)
                .sequence(index)
                .type(type)
                .is_read(0)
                .build();
        alert = alertRepository.save(alert);
        return AlertResponseDto.builder()
                .id(alert.getId())
                .senderId(alert.getSender().getId())
                .senderName(alert.getSender().getNickname())
                .receiverId(alert.getReceiver().getId())
                .receiverName(alert.getReceiver().getNickname())
                .type(alert.getType())
                .index(alert.getSequence())
                .message(alert.getMessage())
                .createdTime(alert.getCreatedDate())
                .build();
    }
}
