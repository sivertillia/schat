package com.sivert.chat.Model;

public class Chat {

    private String sender;
    private String receiver;
    private String message;
    private boolean isseen;
    private String img;
    private String toAnswer;

    public Chat(String sender, String receiver, String message, boolean isseen, String img, String toAnswer) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen = isseen;
        this.img = img;
        this.toAnswer = toAnswer;
    }

    public Chat() {
    }

    public String getSender() { return sender; }

    public void setSender(String sender) { this.sender = sender; }

    public String getReceiver() { return receiver; }

    public void setReceiver(String receiver) { this.receiver = receiver; }

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }

    public boolean isIsseen() { return isseen; }

    public void setIsseen(boolean isseen) { this.isseen = isseen; }

    public String getImg() { return img; }

    public void setImg(String img) { this.img = img; }

    public String getToAnswer() { return toAnswer; }

    public void setToAnswer(String toAnswer) { this.toAnswer = toAnswer; }
}
