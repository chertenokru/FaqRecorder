package faqrecorder.telegram;

import com.vdurmont.emoji.EmojiParser;
import faqrecorder.config.Config;
import faqrecorder.dao.ChatConfig;
import faqrecorder.dao.FaqDbDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import ru.chertenok.telegrambot.AbstractBotCommand;
import ru.chertenok.telegraph.TextPublisher;

import java.util.*;

public class BotCommand extends AbstractBotCommand {
    private static final Logger logger = LoggerFactory.getLogger(BotCommand.class);
    /**
     * Data interfaces to bd
     */
    private static FaqDbDao faqDbDao;
    /**
     * interface to public service in web
     */
    private static TextPublisher textPublisher;
    /**
     * temp list to long message
     */
    private Map<Long, LongMessage> addList = new HashMap<>();

    /**
     * init and start bot
     *
     * @param faqDbDao
     * @param textPublisher
     * @return
     */
    public static BotCommand init(FaqDbDao faqDbDao, TextPublisher textPublisher) {

        BotCommand.faqDbDao = faqDbDao;
        BotCommand.textPublisher = textPublisher;
        logger.info("Init telegram bot api...");
        ApiContextInitializer.init(); // Инициализируем апи
        TelegramBotsApi botapi = new TelegramBotsApi();
        try {
            logger.debug("Create bot ...");
            BotCommand b = new BotCommand();
            logger.debug("Registration bot ...");
            botapi.registerBot(b);
            logger.debug("Registration bot ok");
            return b;

        } catch (TelegramApiException e) {

            logger.error("Bot registration error : ", e);
        }

        return null;

    }

    /**
     * Message Command Handler
     *
     * @param msg
     */
    @Override
    protected void processMessage(Message msg) {
        //    if (msg.getChatId() == -1001197875491L) return;
        if (logger.isDebugEnabled()) logger.debug("processing message...", msg);

        String txt = msg.getText();
        String[] text = txt.split(" ");
        String command = text[0].split("@")[0];
        if (logger.isDebugEnabled()) logger.debug(command, msg);


        switch (command) {
            case "/start": {
                startCommand(msg);
                break;
            }
            case "/help": {
                helpCommand(msg);
                break;
            }
            case "/add": {
                addCommand(msg);
                break;
            }
            case "/add_link": {
                addLinkCommand(msg);
                break;
            }
            case "/done": {
                addDoneCommand(msg);
                break;
            }

            case "/list": {
                listCommand(msg);
                break;
            }
            case "/setup_MessageAsLink_on": {
                setupAsLinkCommand(msg, true);
                break;
            }
            case "/setup_MessageAsLink_off": {
                setupAsLinkCommand(msg, false);
                break;
            }
            default: {
                String[] text1 = command.split("_");
                switch (text1[0]) {
                    case "/view": {
                        if (text1.length > 1) viewCommand(msg, text1[1]);
                        break;
                    }
                    // multi citation?
                    default: {
                        checkMultiCitationCommand(msg);
                    }
                }
            }
        }
    }

    private void addLinkCommand(Message msg) {
        String title;
        String txt = msg.getText();
        String[] text = msg.getText().split(" ");
        long chatId = msg.getChatId();
        if (logger.isDebugEnabled()) logger.debug("/add_link processing..., chatID:" + chatId, msg);
        if (logger.isInfoEnabled()) logger.info("/add_link, chatID:" + chatId);

        if (text.length > 2) {

            String url = null;
            url = text[1];
            title = String.copyValueOf(txt.toCharArray(), text[0].length() + text[1].length() + 2, txt.length() - text[0].length() - text[1].length() - 2);

            faqDbDao.addMessage(chatId, msg.getMessageId(), title, url, true);

            sendMsg(chatId, " Сохранено как <b>" + title + "</b>", null);
            if (logger.isInfoEnabled()) logger.info("/add_link , chatID:" + chatId);
            if (logger.isDebugEnabled()) logger.debug("/add_link processing..., chatID:" + chatId, msg);
        } else {
            sendMsg(chatId, "<b> Сообщение не добавлено, используйте  /help чтобы узнать параметры  </b>", null);
            if (logger.isDebugEnabled()) logger.debug("/add_link not param..., chatID:" + chatId, msg);
            if (logger.isInfoEnabled()) logger.info("/add_link, not param! chatID:" + chatId);
        }


    }

    private void helpCommand(Message msg) {
        long chatId = msg.getChatId();
        if (logger.isDebugEnabled())
            logger.debug("/help chatID:" + chatId, msg);
        if (logger.isInfoEnabled()) logger.info("/help chatID:" + chatId);
        sendMsg(chatId, Config.HELP_TEXT, null);
    }

    private void checkMultiCitationCommand(Message msg) {
        long chatId = msg.getChatId();
        if (logger.isDebugEnabled())
            logger.debug("long message chatID:" + chatId, msg);
        LongMessage longMessage = addList.get(chatId);
        if (logger.isDebugEnabled())
            logger.debug("long message is null?  chatID:" + chatId, longMessage);
        if (longMessage != null) {
            if (logger.isDebugEnabled())
                logger.debug("long message not null ", longMessage);


            if (msg.getForwardFrom() == null || longMessage.userID != msg.getFrom().getId()) {
                addList.remove(chatId);
                if (logger.isDebugEnabled())
                    logger.debug("long message delete ", longMessage);
            } else {
                longMessage.messages.add(msg.getText());
                if (logger.isInfoEnabled()) logger.info("/LongMessage add text  chatID:" + chatId);
                if (logger.isDebugEnabled())
                    logger.debug("long message add text ", longMessage);
            }
        }
    }

    private void setupAsLinkCommand(Message msg, boolean b) {
        long chatId = msg.getChatId();
        if (logger.isDebugEnabled())
            logger.debug("/setupAsLinkCommand: " + (b ? "on" : "off") + " chatID:" + chatId, msg);
        if (logger.isInfoEnabled()) logger.info("/setupAsLinkCommand: " + (b ? "on" : "off") + " chatID:" + chatId);

        try {
            ChatConfig config = faqDbDao.getChatConfigByID(chatId);
            config.isPublicInTelegraph = b;
            faqDbDao.setChatConfigByID(chatId, config);
            sendMsg(chatId, "Настройки изменены", null);
            if (logger.isDebugEnabled())
                logger.debug("/setupAsLinkCommand done   chatID:" + chatId, msg);

        } catch (Exception e) {
            sendMsg(chatId, "не удалось изменить настройки", null);
            logger.error("Set setting error  chatID:" + chatId, msg);
        }
    }

    private void viewCommand(Message msg, String s1) {
        long chatId = msg.getChatId();
        if (logger.isDebugEnabled()) logger.debug("/list_xxx processing..., chatID:" + chatId, msg, s1);
        if (logger.isInfoEnabled()) logger.info("/view_xxx: " + s1 + ", chatID:" + chatId);

        String s = null;
        try {
            int recNo = Integer.valueOf(s1.split("@")[0]);
            s = escapeHTML(faqDbDao.getMessageById(chatId, recNo));
        } catch (Exception e) {
            logger.error("Command view_xxx error, chatID:" + chatId, msg);
        }
        if (s == null) {
            s = "<i>Такая запись не найдена</i>";
        }
        sendMsg(msg.getChatId(), s, null);
        if (logger.isDebugEnabled()) logger.debug("Command view_xxx done, chatID:" + chatId, s);
    }

    private void listCommand(Message msg) {
        long chatId = msg.getChatId();
        if (logger.isDebugEnabled()) logger.debug("/list processing..., chatID:" + chatId, msg);
        if (logger.isInfoEnabled()) logger.info("/list, chatID:" + chatId);


        Map<Integer, Map<String, String>> map = faqDbDao.getMessageListByCatId(chatId);
        //  Map<Integer, Map<String,String>> map = faqDbDao.getMessageListByCatId(-1001197875491L);

        String s = EmojiParser.parseToUnicode(Config.LIST_TEXT);
        InlineKeyboardMarkup buttons = null;
        if (map != null) {

            for (Map.Entry<Integer, Map<String, String>> entry : map.entrySet()) {
                Map<String, String> mapIn = entry.getValue();
                if (Boolean.valueOf(mapIn.get("isLink"))) {
                    Map<String, String> maps = new HashMap<String, String>();
                    maps.put(mapIn.get("name"), mapIn.get("message"));
                    buttons = getButtonList(buttons, maps, true);
                } else {
                    s += EmojiParser.parseToUnicode("<i>  :small_orange_diamond: " + entry.getValue().get("name") + "</i>    /view_" + entry.getKey() + "\n");
                }
            }

        } else {
            s += "➖ Записей нет";
        }

        sendMsg(chatId, s, buttons);
    }

    private void addCommand(Message msg) {
        String title;
        String txt = msg.getText();
        String[] text = msg.getText().split(" ");
        long chatId = msg.getChatId();
        if (logger.isDebugEnabled()) logger.debug("/add processing..., chatID:" + chatId, msg);
        if (logger.isInfoEnabled()) logger.info("/add, chatID:" + chatId);

        // simple citation
        if (msg.getReplyToMessage() != null) {


            if (text.length == 1) {
                title = "строка";
            } else {

                title = String.copyValueOf(txt.toCharArray(), text[0].length(), txt.length() - text[0].length());
            }

            String message = null;
            // get chat config
            ChatConfig chatConfig = faqDbDao.getChatConfigByID(chatId);
            if (chatConfig.isPublicInTelegraph) {
                try {
                    message = textPublisher.getUrl(Arrays.asList(msg.getReplyToMessage().getText()), title, msg.getReplyToMessage().getAuthorSignature());
                    if (logger.isDebugEnabled()) logger.debug("/add url , chatID:" + chatId, message);
                } catch (Exception e) {
                    logger.error("Error telegra.ph posting, chatID:" + chatId, e, msg);
                }
            } else {
                message = msg.getReplyToMessage().getText();
                if (logger.isDebugEnabled()) logger.debug("/add text , chatID:" + chatId, message);
            }

            if (message != null) {
                faqDbDao.addMessage(chatId, msg.getMessageId(), title, message, chatConfig.isPublicInTelegraph);

                sendMsg(chatId, " Сохранено как <b>" + title + "</b>", null);
                if (logger.isInfoEnabled()) logger.info("/add simple , chatID:" + chatId);
            } else {
                sendMsg(chatId, "<b> Сообщение не добавлено </b>", null);
                if (logger.isDebugEnabled()) logger.debug("/add message is null..., chatID:" + chatId, msg);
                if (logger.isInfoEnabled()) logger.info("/add, message is null! chatID:" + chatId);
            }

        } else
        // multi citation ?
        {
            if (logger.isDebugEnabled()) logger.debug("/add init multi citation..., chatID:" + chatId, msg);
            if (logger.isInfoEnabled()) logger.info("/add, init multi citation! chatID:" + chatId);
            addList.remove(chatId);
            LongMessage longMessage = new LongMessage();
            longMessage.messages = new ArrayList<>();
            if (text.length == 1) {
                longMessage.title = "запись";
            } else {
                longMessage.title = String.copyValueOf(txt.toCharArray(), text[0].length() + 1, txt.length() - text[0].length() - 1);
            }
            longMessage.userID = msg.getFrom().getId();
            addList.put(chatId, longMessage);
        }
    }

    private void addDoneCommand(Message msg) {
        long chatId = msg.getChatId();
        if (logger.isDebugEnabled()) logger.debug("/done , chatID:" + chatId, msg);
        if (logger.isInfoEnabled()) logger.info("/done , chatID:" + chatId);

        LongMessage longMessage = addList.get(chatId);
        if (longMessage != null) {
            if (!longMessage.messages.isEmpty()) {
                String message = null;
                // get chat config
                ChatConfig chatConfig = faqDbDao.getChatConfigByID(msg.getChatId());
                if (chatConfig.isPublicInTelegraph) {
                    try {
                        message = textPublisher.getUrl(longMessage.messages, longMessage.title, msg.getAuthorSignature());
                        if (logger.isDebugEnabled()) logger.debug("/add url , chatID:" + chatId, message);
                    } catch (Exception e) {
                        logger.error("Error telegra.ph posting, chatID:" + chatId, e, msg);
                    }
                } else
                    message = longMessage.messages.toString();


                if (message != null) {
                    faqDbDao.addMessage(chatId, msg.getMessageId(), longMessage.title, message, chatConfig.isPublicInTelegraph);

                    sendMsg(chatId, " Сохранено как <b>" + longMessage.title + "</b>", null);
                    if (logger.isInfoEnabled()) logger.info("/done multi citation , chatID:" + chatId);
                } else {
                    sendMsg(chatId, "<b> Сообщение не добавлено </b>", null);
                    if (logger.isDebugEnabled()) logger.debug("/done, message is null..., chatID:" + chatId, msg);
                    if (logger.isInfoEnabled()) logger.info("/done, message is null! chatID:" + chatId);
                }
                addList.remove(chatId);
            }
        }

    }

    private void startCommand(Message msg) {
        long chatId = msg.getChatId();
        if (logger.isDebugEnabled()) logger.debug("/start, chatID:" + chatId, msg);
        if (logger.isInfoEnabled()) logger.info("/start, chatID:" + chatId);

        String s = faqDbDao.getChatNameByID(chatId);
        String stat = "";

        if (s != null) {
            stat = "уже ";
        } else {
            s = Config.BOOK_NAME_DEFAULT;
            if (faqDbDao.addNewChat(chatId, s)) {
                if (logger.isDebugEnabled()) logger.debug("/start, add chat done, chatID:" + chatId, msg);
                if (logger.isInfoEnabled()) logger.info("/start, add chat done, chatID:" + chatId);

            } else {
                logger.error("/start chat not created bd error chatid:" + chatId, msg);
            }


        }

        sendMsg(chatId, String.format(Config.START_MESSAGE, getUserName(msg.getFrom()), msg.getChat().getTitle(), stat, s),
                null);

    }

    /**
     * message to new user
     *
     * @param message
     */
    @Override
    protected void processNewUsers(Message message) {
        long chatId = message.getChatId();
        if (logger.isDebugEnabled()) logger.debug("new user, chatID:" + chatId, message);
        if (logger.isInfoEnabled()) logger.info("new user, chatID:" + chatId);

        String users = "";
        for (User user : message.getNewChatMembers()) {
            if (!user.getBot()) {
                if (users.length() > 0) users += ", ";

                users += getUserName(user);
            }
        }

        if (users.length() > 0) {
            sendMsg(chatId, String.format(Config.WELCOME_MESSAGE, users), null);
            listCommand(message);
        }
    }

    /**
     * On button click event
     *
     * @param callbackQuery
     */
    @Override
    protected void processCallbackQuery(CallbackQuery callbackQuery) {
//        sendCallMsg(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId(),
//                EmojiParser.parseToUnicode(callbackQuery.getFrom().getFirstName() + ", ну хватит кнопки жать :stuck_out_tongue_winking_eye:")
//                , null);

    }

    @Override
    public String getBotUsername() {
        return Config.TELEGRAM_LOGIN;
    }

    @Override
    public String getBotToken() {
        return Config.getTelegramToken();
    }

    private class LongMessage {
        public long userID;
        public String title;
        public List<String> messages = new ArrayList<>();
    }


}