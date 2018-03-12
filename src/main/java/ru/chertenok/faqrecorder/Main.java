package ru.chertenok.faqrecorder;

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

    public static void main(String[] args) {
        // bd init
        FaqDbDaoImplSQLite faqDbDaoImplSQLite = new FaqDbDaoImplSQLite();
        // telegra.ph init
        TextPublisher textPublisher = new TelegraphPublisher(faqDbDaoImplSQLite);
        // init & start bot
        BotCommand.init(faqDbDaoImplSQLite, textPublisher);
    }
}
