package ru.chertenok.faqrecorder.telegrambot;

import com.vdurmont.emoji.EmojiParser;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import ru.chertenok.faqrecorder.config.Config;
import ru.chertenok.faqrecorder.dao.ChatConfig;
import ru.chertenok.faqrecorder.dao.FaqDbDao;
import ru.chertenok.faqrecorder.telegraph.TextPublisher;

import java.util.*;

public class BotCommand extends AbstractBotCommand {

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

        ApiContextInitializer.init(); // Инициализируем апи
        TelegramBotsApi botapi = new TelegramBotsApi();
        try {
            BotCommand b = new BotCommand();
            botapi.registerBot(b);
//            moveRecordToTelegraph(224632793);
            return b;

        } catch (TelegramApiException e) {
            e.printStackTrace();
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

        String txt = msg.getText();
        String[] text = txt.split(" ");
        String command = text[0].split("@")[0];


        switch (command) {
            case "/start": {
                startCommand(msg);
                break;
            }
            case "/add": {
                addCommand(msg);
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
                String[] text1 = txt.split("_");
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

    private void checkMultiCitationCommand(Message msg) {
        long chatId = msg.getChatId();
        LongMessage longMessage = addList.get(chatId);
        if (longMessage != null) {
            if (msg.getForwardFrom() == null || longMessage.userID != msg.getFrom().getId()) {
                addList.remove(chatId);
            } else {
                longMessage.messages.add(msg.getText());
            }
        }
    }

    private void setupAsLinkCommand(Message msg, boolean b) {
        long chatId = msg.getChatId();
        try {
            ChatConfig config = faqDbDao.getChatConfigByID(chatId);
            config.isPublicInTelegraph = b;
            faqDbDao.setChatConfigByID(chatId, config);
            sendMsg(chatId, "Настройки изменены", null);
        } catch (Exception e) {
            sendMsg(chatId, "не удалось изменить настройки", null);
            e.printStackTrace();
        }
    }

    private void viewCommand(Message msg, String s1) {
        String s = faqDbDao.getMessageById(msg.getChatId(), Integer.valueOf((s1.split("@"))[0]));
        if (s == null) {
            s = "<i>Такая запись не найдена</i>";
        }
        sendMsg(msg.getChatId(), s, null);
    }

    private void listCommand(Message msg) {
        Map<Integer, Map<String, String>> map = faqDbDao.getMessageListByCatId(msg.getChatId());
        //  Map<Integer, Map<String,String>> map = faqDbDao.getMessageListByCatId(-1001197875491L);
        String s = EmojiParser.parseToUnicode(":books: <b>Сохранённые записи:</b>\n");
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

        sendMsg(msg.getChatId(), s, buttons);
    }

    private void addCommand(Message msg) {
        String title;
        String txt = msg.getText();
        String[] text = msg.getText().split(" ");
        long chatId = msg.getChatId();
        // simple citation
        if (msg.getReplyToMessage() != null) {
            if (text.length == 1) {
                title = "строка";
            } else {

                title = String.copyValueOf(txt.toCharArray(), text[0].length(), txt.length() - text[0].length());
            }

            String message;
            // get chat config
            ChatConfig chatConfig = faqDbDao.getChatConfigByID(chatId);
            if (chatConfig.isPublicInTelegraph) {
                message = textPublisher.getUrl(Arrays.asList(msg.getReplyToMessage().getText()), title, msg.getReplyToMessage().getAuthorSignature());
            } else
                message = msg.getReplyToMessage().getText();

            faqDbDao.addMessage(chatId, msg.getMessageId(), title, message, chatConfig.isPublicInTelegraph);

            sendMsg(chatId, " Сохранено как <b>" + title + "</b>", null);
        } else
        // multi citation ?
        {
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
        LongMessage longMessage = addList.get(chatId);
        if (longMessage != null) {
            if (!longMessage.messages.isEmpty()) {
                // get chat config
                ChatConfig chatConfig = faqDbDao.getChatConfigByID(msg.getChatId());
                String message;
                if (chatConfig.isPublicInTelegraph) {
                    message = textPublisher.getUrl(longMessage.messages, longMessage.title, msg.getAuthorSignature());
                } else
                    message = longMessage.messages.toString();

                faqDbDao.addMessage(msg.getChatId(), msg.getMessageId(), longMessage.title, message, chatConfig.isPublicInTelegraph);

                sendMsg(msg.getChatId(), " Сохранено как <b>" + longMessage.title + "</b>", null);
                addList.remove(chatId);
            }
        }

    }

    private void startCommand(Message msg) {
        String s = faqDbDao.getChatNameByID(msg.getChatId());

        if (s != null) {
            sendMsg(msg.getChatId(), getUserName(msg.getFrom()) + ", к чату " + msg.getChat().getTitle() + ", уже подключена записная книга - "
                    + s, null);
        } else {
            s = "Памятка";
            faqDbDao.addNewChat(msg.getChatId(), s);
            sendMsg(msg.getChatId(), getUserName(msg.getFrom()) + ", к чату " + msg.getChat().getTitle() + ", подключена записная книга - "
                    + s, null);
        }
    }

    /**
     * message to new user
     *
     * @param message
     */
    @Override
    protected void processNewUsers(Message message) {

        String users = "";
        for (User user : message.getNewChatMembers()) {
            if (!user.getBot()) {
                if (users.length() > 0) users += ", ";

                users += getUserName(user);
            }
        }

        if (users.length() > 0) {
            sendMsg(message.getChatId(), String.format(Config.WELCOME_MESSAGE, users), null);
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

        sendCallMsg(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId(),
                EmojiParser.parseToUnicode(callbackQuery.getFrom().getFirstName() + ", ну хватит кнопки жать :stuck_out_tongue_winking_eye:")
                , null);

    }

    private class LongMessage {
        public long userID;
        public String title;
        public List<String> messages = new ArrayList<>();
    }
}