package faqrecorder;

import faqrecorder.config.Config;
import faqrecorder.dao.FaqDbDaoImplSQLite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.chertenok.telegrambot.telegrambot.BotCommand;
import ru.chertenok.telegraph.TelegraphPublisher;
import ru.chertenok.telegraph.TextPublisher;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;


// todo начальное создание БД
// todo сброс БД
// todo проверка админских прав
// todo удаление, изменение записей
// todo logs to all, разобраться с логером и настроить нормально и все модули
// todo управление выбранным чатом через личную переписку с ботом, а не в форуме
// todo телеграф, нормальное формирование тэгов, не по словам а фразами
// todo проверку перед каждой командой, что чат уже создан, закэшировать в список?
// todo команды настройки - приветственное сообщение для нового юзера, надо ли его выводить
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        try {

            java.util.logging.Logger.getGlobal().setLevel(Config.LOG_GLOBAL_LEVEL);

            FileHandler handler = new FileHandler(Config.LOG_FILE_NAME, Config.LOG_FILE_SIZE, 1, true);
            handler.setLevel(Config.LOG_FILE_LEVEL);
            handler.setFormatter(new SimpleFormatter());
            java.util.logging.Logger.getGlobal().addHandler(handler);
        } catch (IOException e) {
            java.util.logging.Logger.getGlobal().log(Level.SEVERE, "log file '" + Config.LOG_FILE_NAME + "'not created :" + e);
        }



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
