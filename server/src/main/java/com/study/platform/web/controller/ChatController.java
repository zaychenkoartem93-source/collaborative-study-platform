package com.study.platform.web.controller;

import com.study.platform.web.dto.ChatMessageDto;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat/{groupId}")
    public void handleChatMessage(@DestinationVariable Long groupId,
                                  ChatMessageDto incoming) {

        // Enrich message on server side
        ChatMessageDto outgoing = new ChatMessageDto();
        outgoing.setGroupId(groupId);
        outgoing.setSender(incoming.getSender());
        outgoing.setText(incoming.getText());
        outgoing.setSentAt(OffsetDateTime.now().toString());

        messagingTemplate.convertAndSend(
                "/topic/groups/" + groupId + "/chat",
                outgoing
        );
    }
}
