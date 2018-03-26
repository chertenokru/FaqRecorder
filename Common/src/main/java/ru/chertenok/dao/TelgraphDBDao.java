package ru.chertenok.dao;

public interface TelgraphDBDao {

    String getAuthor();

    String getToken();

    boolean addAuthor(String authorAcc, String author, String token);

}
