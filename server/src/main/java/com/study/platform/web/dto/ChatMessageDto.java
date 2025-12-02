package com.study.platform.web.dto;

public class ChatMessageDto {

    private Long groupId;
    private String sender;   // display name or email
    private String text;
    private String sentAt;   // ISO string

    public ChatMessageDto() {}

    public ChatMessageDto(Long groupId, String sender, String text, String sentAt) {
        this.groupId = groupId;
        this.sender = sender;
        this.text = text;
        this.sentAt = sentAt;
    }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getSentAt() { return sentAt; }
    public void setSentAt(String sentAt) { this.sentAt = sentAt; }
}
