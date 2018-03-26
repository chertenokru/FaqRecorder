package ru.chertenok.dao.Impl;

import faqrecorder.config.Config;
import ru.chertenok.bd.sqlite.BDHandler;
import ru.chertenok.dao.ChatConfig;
import ru.chertenok.dao.DepricatedFaqDbDao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public class FaqDbDaoImplSQLite implements DepricatedFaqDbDao {


    public FaqDbDaoImplSQLite() {
        try {
            BDHandler.init(Config.CONNECT_TO_BD_STRING, Config.PATH_TO_BD);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void closeBD() {
        try {
            BDHandler.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public String getChatNameByID(long chatId) {
        try {
            String s = BDHandler.getFirstStringFromSelect("select name from chats where chatID = ?", String.valueOf(chatId));
            return s;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ChatConfig getChatConfigByID(long chatId) {
        try {
            ResultSet rs = BDHandler.getResultSetFromSelect("select name,chatID,IsPublicInTelegraph from chats where chatID = ?", String.valueOf(chatId));
            ChatConfig config = new ChatConfig(rs.getInt(2), rs.getString(1), rs.getBoolean(3));
            rs.close();
            return config;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public boolean setChatConfigByID(long chatId, ChatConfig newConfig) {
        try {
            BDHandler.executeQueryWithNoResult("update chats set name =? , IsPublicInTelegraph = ?  where chatID = ?",
                    newConfig.Name, newConfig.isPublicInTelegraph ? "1" : "0", String.valueOf(chatId));
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean addNewChat(long chatId, String chatFaqName) {
        try {
            BDHandler.executeQueryWithNoResult("insert into chats(chatID,name) values (?,?) ", String.valueOf(chatId), chatFaqName);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

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
    public String getAuthor() {
        try {
            String s = BDHandler.getFirstStringFromSelect("select author from telegraph");
            return s;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String getToken() {
        try {
            String s = BDHandler.getFirstStringFromSelect("select token from telegraph");
            return s;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean addAuthor(String authorAcc, String author, String token) {
        try {
            BDHandler.executeQueryWithNoResult("insert into telegraph(author_acc,author,token) values (?,?,?) ",
                    authorAcc, author, token);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
