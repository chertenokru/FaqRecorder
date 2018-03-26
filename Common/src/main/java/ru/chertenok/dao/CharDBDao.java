package ru.chertenok.dao;

public interface CharDBDao {
    // return null if chat not exist
    String getChatNameByID(long chatId);

    boolean setChatConfigByID(long chatId, ChatConfig newChatConfig);

    ChatConfig getChatConfigByID(long chatId);

    boolean addNewChat(long chatId, String chatFaqName);
}
