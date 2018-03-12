package ru.chertenok.faqrecorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.chertenok.faqrecorder.dao.FaqDbDaoImplSQLite;
import ru.chertenok.faqrecorder.telegrambot.BotCommand;
import ru.chertenok.faqrecorder.telegraph.TelegraphPublisher;
import ru.chertenok.faqrecorder.telegraph.TextPublisher;


// todo начальное создание БД
// todo сброс БД
// todo проверка админских прав
// todo удаление, изменение записей
// todo logs to all
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        logger.info("Start App...");
        logger.info("DAO init start...");
        // bd init
        FaqDbDaoImplSQLite faqDbDaoImplSQLite = new FaqDbDaoImplSQLite();
        logger.info("textPublisher init start...");
        // telegra.ph init
        TextPublisher textPublisher = new TelegraphPublisher(faqDbDaoImplSQLite);
        logger.info("Bot init start...");
        // init & start bot
        BotCommand.init(faqDbDaoImplSQLite, textPublisher);
        logger.info("app started done");
    }
}
