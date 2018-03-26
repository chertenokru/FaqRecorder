package ru.chertenok.dao.Impl;

import ru.chertenok.bd.sqlite.BDHandler;
import ru.chertenok.dao.MessageDbDao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MessageDbDaoImplSQLite implements MessageDbDao {


    @Override
    public boolean addMessage(long chatId, int messageId, String name, String message, boolean isLink) {
        try {
            BDHandler.executeQueryWithNoResult("insert into messages(id,chatid,name,messageid,message,isLink) values (?,?,?,?,?,?) ",
                    String.valueOf(messageId), String.valueOf(chatId), name, String.valueOf(messageId), message, isLink ? "1" : "0");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean deleteMessageById(long chatId, int messageId) {
        return false;
    }

    @Override
    public boolean renameMessageById(long chatId, int messageId, String newName) {
        return false;
    }


    @Override
    public String getMessageById(long chatId, int messageId) {

        try {

            String s = BDHandler.getFirstStringFromSelect("select message from messages where chatid = ? and messageid = ?", String.valueOf(chatId), String.valueOf(messageId));

            return s;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Map<Integer, Map<String, String>> getMessageListByCatId(long chatId) {
        Map<Integer, Map<String, String>> map = new HashMap<>();
        try {
            ResultSet rs = BDHandler.getResultSetFromSelect("select * from messages where chatid = ?", String.valueOf(chatId));
            if (rs != null) {
                while (rs.next()) {
                    Map<String, String> mapIn = new HashMap<>();
                    mapIn.put("name", rs.getString("name"));
                    mapIn.put("message", rs.getString("message"));
                    mapIn.put("isLink", String.valueOf(rs.getBoolean("isLink")));
                    map.put(rs.getInt("id"), mapIn);
                }
                rs.close();
                rs.getStatement().close();
                return map;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean renameMessageBodyById(long chatID, Integer messageID, String text, boolean isLink) {
        try {
            BDHandler.executeQueryWithNoResult("update messages set message = ? where chatid =? and id = ? and isLink = ?", text, String.valueOf(chatID), String.valueOf(messageID), isLink ? "1" : "0");
        } catch (SQLException e) {
            return true;
        }
        return false;
    }

}
