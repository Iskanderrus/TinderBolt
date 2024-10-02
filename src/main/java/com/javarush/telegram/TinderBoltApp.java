package com.javarush.telegram;

import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String OPEN_AI_TOKEN = loadEnvVariable("CHAT_GPT_TOKEN");

    private final ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode currentMode = null;
    private ArrayList<String> list = new ArrayList<>();

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

            showMainMenu("Главное меню бота", "/start", "Генерация Tinder-профля", "/profile", "Сообщение для знакомства", "/opener", "Переписка от вашего имени", "/message", "Переписка со звездами", "/date", "Общение с GPT", "/gpt");


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
            Message msg = sendHtmlMessage("Подождите пару секунд - ChatGPT думает...");
            String answer = chatGPT.sendMessage(prompt, message);
            updateTextMessage(msg, answer);
            return;
        }
        // command DATE
        if (message.equals("/date")) {
            currentMode = DialogMode.DATE;
            String text = loadMessage("date");
            sendTextButtonsMessage(text, "Ариана Гранде", "date_grande", "Марго Робби", "date_roddie", "Зендея", "date_zendaya", "Райн Гослинг", "date_gosling", "Том Харди", "date_hardy");
            return;
        }

        if (currentMode == DialogMode.DATE) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("date_")) {
                sendPhotoMessage(query);
                sendTextMessage("Отличный выбор!\nТвоя зачада пригласить парня/девушку на свидание ❤\uFE0F за 5 сообщений!");

                String prompt = loadPrompt(query);
                chatGPT.setPrompt(prompt);
                return;
            }
            Message msg = sendHtmlMessage("Подождите, идет набор текста...");
            String answer = chatGPT.addMessage(message);
            updateTextMessage(msg, answer);
            return;
        }

        // command MESSAGE
        if (message.equals("/message")) {
            currentMode = DialogMode.MESSAGE;
            sendPhotoMessage("message");
            sendTextButtonsMessage("Пришлите в чат Вашу переписку", "Следующее сообщение", "message_next", "Пригласить на свидание", "message_date");
            return;
        }
        if (currentMode == DialogMode.MESSAGE) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("message_")) {
                String prompt = loadPrompt(query);
                String userChatHistory = String.join("\n\n", list);
                Message msg = sendHtmlMessage("Подождите пару секунд - ChatGPT думает...");
                String answer = chatGPT.sendMessage(prompt, userChatHistory);
                updateTextMessage(msg, answer);
                return;
            }
            list.add(message);
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
