package com.javarush.telegram;

import com.javarush.telegram.ChatGPTService;
import com.javarush.telegram.DialogMode;
import com.javarush.telegram.MultiSessionTelegramBot;
import com.javarush.telegram.UserInfo;
import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {

    public TinderBoltApp() {
        super(
                loadEnvVariable("TELEGRAM_BOT_NAME"),
                loadEnvVariable("TELEGRAM_BOT_TOKEN")
        );
    }

    private static String loadEnvVariable(String key) {
        Dotenv dotenv = Dotenv.load();
        return dotenv.get(key);
    }

    @Override
    public void onUpdateEventReceived(Update update) {

        String message = getMessageText();

        if (message.equals("/start")){
            sendPhotoMessage("main");
            String text = loadMessage("main");
            sendTextMessage(text);
            return;
        }

        sendTextMessage("*Привет!*");
        sendTextMessage("_Привет!_");
        sendTextMessage("В написали " + message);

        sendTextButtonsMessage(
                "Выберите режим работы: ",
                "Старт", "start",
                "Стоп", "stop"
        );

    }

    @Override
    public String getBotToken() {
        return loadEnvVariable("TELEGRAM_BOT_TOKEN");
    }

    @Override
    public String getBotUsername() {
        return loadEnvVariable("TELEGRAM_BOT_NAME");
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
