package com.simplechatter.chatactivity;

public class Message {
    private String from,message,type,to,messageId,date,time,name,groupId;

    public Message() {
    }


    public Message(String from, String message, String type, String to, String messageId, String date, String time, String name) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.to = to;
        this.messageId = messageId;
        this.date = date;
        this.time = time;
        this.name = name;
    }

    public Message(String from, String message, String type, String to, String messageId, String date, String time, String name, String groupId) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.to = to;
        this.messageId = messageId;
        this.date = date;
        this.time = time;
        this.name = name;
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Message{" +
                "from='" + from + '\'' +
                ", message='" + message + '\'' +
                ", type='" + type + '\'' +
                ", to='" + to + '\'' +
                ", messageId='" + messageId + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
