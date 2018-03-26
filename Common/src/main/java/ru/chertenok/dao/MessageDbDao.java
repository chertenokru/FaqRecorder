package ru.chertenok.dao;

import java.util.Map;

public interface MessageDbDao {

    boolean addMessage(long chatId, int messageId, String name, String message, boolean isLink);

    boolean deleteMessageById(long chatId, int messageId);

    boolean renameMessageById(long chatId, int messageId, String newName);

    String getMessageById(long chatId, int messageId);

    Map<Integer, Map<String, String>> getMessageListByCatId(long chatId);

    boolean renameMessageBodyById(long chatID, Integer key, String text, boolean IsLink);
}
