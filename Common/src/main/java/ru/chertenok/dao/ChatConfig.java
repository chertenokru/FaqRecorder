package ru.chertenok.dao;

public class ChatConfig {
    public long chatID;
    public String Name;
    public boolean isPublicInTelegraph;

    public ChatConfig(long chatID, String name, boolean isPublicInTelegraph) {
        this.chatID = chatID;
        Name = name;
        this.isPublicInTelegraph = isPublicInTelegraph;
    }
}
