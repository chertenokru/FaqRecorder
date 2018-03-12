package ru.chertenok.faqrecorder.telegrambot;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import ru.chertenok.faqrecorder.config.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public abstract class AbstractBotCommand extends TelegramLongPollingBot {


    @Override
    public String getBotUsername() {
        return Config.TELEGRAM_LOGIN;
    }


    @Override
    public String getBotToken() {
        return Config.getTelegramToken();
    }


    /**
     * All events handler
     * Called when an event is received from telegram
     *
     * @param update
     */
    @Override
    public void onUpdateReceived(Update update) {
        // event new user
        if (update.hasMessage() && update.getMessage().getNewChatMembers() != null)
            processNewUsers(update.getMessage());
        // event new message
        if (update.hasMessage() && update.getMessage().hasText()) processMessage(update.getMessage());
        // event button click
        if (update.hasCallbackQuery()) processCallbackQuery(update.getCallbackQuery());

    }

    protected abstract void processCallbackQuery(CallbackQuery callbackQuery);

    protected abstract void processMessage(Message message);

    protected abstract void processNewUsers(Message message);


    /**
     * add new message
     *
     * @param chatId
     * @param text
     * @param markupInline - button in message
     */
    public void sendMsg(long chatId, String text, InlineKeyboardMarkup markupInline) {
        SendMessage s = new SendMessage();
        s.enableHtml(true).disableNotification();

        s.setChatId(chatId); // Боту может писать не один человек, и поэтому чтобы отправить сообщение, грубо говоря нужно узнать куда его отправлять
        s.setText(text);
        if (markupInline != null) s.setReplyMarkup(markupInline);

        try { //Чтобы не крашнулась программа при вылете Exception
            execute(s);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public InlineKeyboardMarkup getButtonList(InlineKeyboardMarkup keyboardMarkup, Map<String, String> buttons, boolean isURL) {
        if (keyboardMarkup == null) {
            keyboardMarkup = new InlineKeyboardMarkup();
        }
        List<List<InlineKeyboardButton>> rowsInline = null;
        if (keyboardMarkup.getKeyboard() != null) {
            rowsInline = keyboardMarkup.getKeyboard();
        } else {
            rowsInline = new ArrayList<>();
        }

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        for (Map.Entry<String, String> entry : buttons.entrySet()) {
            if (!isURL) {
                rowInline.add(new InlineKeyboardButton().setText(entry.getKey()).setCallbackData(entry.getValue()));
            } else {
                rowInline.add(new InlineKeyboardButton().setText(entry.getKey()).setUrl(entry.getValue()));
            }
            rowsInline.add(rowInline);
        }
        // Add it
        keyboardMarkup.setKeyboard(rowsInline);

        return keyboardMarkup;
    }

    /**
     * edit exist message
     *
     * @param chatId
     * @param text
     * @param markupInline - button in message
     */
    public void sendCallMsg(long chatId, int messageId, String text, InlineKeyboardMarkup markupInline) {
        EditMessageText s = new EditMessageText();
        s.setChatId(chatId);
        s.setMessageId(messageId);
        s.setText(text);
        if (markupInline != null) {
            s.setReplyMarkup(markupInline);
        }
        try { //Чтобы не крашнулась программа при вылете Exception
            execute(s);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String getUserName(User user) {
        return "" +
                (user.getFirstName() == null ? "" : user.getFirstName() + " ")
                + (user.getLastName() == null ? "" : user.getLastName() + " ")
                + (user.getUserName() == null ? "" : "@" + user.getUserName());
    }

}
