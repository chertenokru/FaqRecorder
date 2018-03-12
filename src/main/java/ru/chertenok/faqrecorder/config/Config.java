package ru.chertenok.faqrecorder.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

// todo external setting file
public class Config {

    public static final String PATH_TO_BD = "faq.db";
    public static final String BD_DRIVER_NAME = "org.sqlite.JDBC";
    public static final String CONNECT_TO_BD_STRING = "jdbc:sqlite:%s";
    public static final String LOG_FILE_NAME = "server_log.txt";
    public static final int LOG_FILE_SIZE = 1024;
    public static final Level LOG_GLOBAL_LEVEL = Level.INFO;
    public static final Level LOG_FILE_LEVEL = Level.ALL;
    public static final String TELEGRAPH_AUTHOR_ACC = "faq_message_recorder";
    public static final String TELEGRAPH_AUTHOR = "Messages Recorder";
    public static final String TELEGRAM_LOGIN = "MessageRecorderBot";

    private static final Properties properties = new Properties();
    public static final String HELP_TEXT =
            "Этот бот умеет сохранять текстовые сообщения в БД или в виде публикаций telegra.ph \n\n"+
            " Список команд: \n"+
            " /start - начало работы \n"+
            " /help - эта команда \n"+
            " /list - список сохранённых сообщений \n"+
            " /setup_MessageAsLink_on - сохранять сообщения в telegra.ph \n"+
            " /setup_MessageAsLink_off - сохранять сообщения в БД \n"+
            " /add описание - сохраняет цитируемое сообщение с указанным описанием.\n "+
            "Для сохранения нескольких сообщений как одно, выделите их  " +
            "и отправьте через команду переслать, добавив  /add описание. " +
            "После этого отправьте команду /done \n " +
             "Если всё прошло хорошо, то Вы получите подтверждающее сообщение. ";


    public static String WELCOME_MESSAGE = "%s, добро пожаловать в наш чат ! \n Ознакомься с основными документами.";

    static {

        try {
            properties.load(new FileInputStream("config.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Config() {
    }


    public static String getTelegramToken() {
        return properties.getProperty("token");
    }
}
