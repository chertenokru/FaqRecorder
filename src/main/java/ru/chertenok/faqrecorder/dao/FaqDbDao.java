package ru.chertenok.faqrecorder.dao;

import java.util.Map;

public interface FaqDbDao {
    // return null if chat not exist
    String getChatNameByID(long chatId);

    boolean setChatConfigByID(long chatId, ChatConfig newChatConfig);

    ChatConfig getChatConfigByID(long chatId);

    boolean addNewChat(long chatId, String chatFaqName);

    boolean addMessage(long chatId, int messageId, String name, String message, boolean isLink);


    boolean deleteMessageById(long chatId, int messageId);

    boolean renameMessageById(long chatId, int messageId, String newName);

    String getAuthor();

    String getToken();

    boolean addAuthor(String authorAcc, String author, String token);

    String getMessageById(long chatId, int messageId);

    Map<Integer, Map<String, String>> getMessageListByCatId(long chatId);


    boolean renameMessageBodyById(long chatID, Integer key, String text, boolean IsLink);
}
