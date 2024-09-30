package com.javarush.telegram;

import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String OPEN_AI_TOKEN = loadEnvVariable("CHAT_GPT_TOKEN");

    private final ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode currentMode = null;

    public TinderBoltApp() {
        super(loadEnvVariable("TELEGRAM_BOT_NAME"), loadEnvVariable("TELEGRAM_BOT_TOKEN"));
    }

    private static String loadEnvVariable(String key) {
        Dotenv dotenv = Dotenv.load();
        return dotenv.get(key);
    }

    @Override
    public void onUpdateEventReceived(Update update) {

        String message = getMessageText();

        if (message.equals("/start")) {
            currentMode = DialogMode.MAIN;
            sendPhotoMessage("main");
            String text = loadMessage("main");
            sendTextMessage(text);

            showMainMenu(
                    "Главное меню бота", "/start",
                    "Генерация Tinder-профля", "/profile",
                    "Сообщение для знакомства", "/opener",
                    "Переписка от вашего имени", "/message",
                    "Переписка со звездами", "/date",
                    "Общение с GPT", "/gpt"
            );


            return;
        }

        if (message.equals("/gpt")) {
            currentMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            String text = loadMessage("gpt");
            sendTextMessage(text);


            return;
        }
        if (currentMode == DialogMode.GPT) {
            String prompt = loadPrompt("gpt");
            String answer = chatGPT.sendMessage(prompt, message);
            sendTextMessage(answer);
            return;
        }

        sendTextMessage("*Привет!*");
        sendTextMessage("_Привет!_");
        sendTextMessage("В написали " + message);

        sendTextButtonsMessage("Выберите режим работы: ", "Старт", "start", "Стоп", "stop");

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
