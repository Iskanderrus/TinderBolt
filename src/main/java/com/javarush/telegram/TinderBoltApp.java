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

    private int questionCount;
    private UserInfo me, she;

    public TinderBoltApp() {
        super(loadEnvVariable("TELEGRAM_BOT_NAME"), loadEnvVariable("TELEGRAM_BOT_TOKEN"));
    }

    private static String loadEnvVariable(String key) {
        Dotenv dotenv = Dotenv.load();
        return dotenv.get(key);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        final String waitText = "Подождите пару секунд - ChatGPT думает \uD83E\uDD2F ...";

        String message = getMessageText();
        // command START
        if (message.equals("/start")) {
            currentMode = DialogMode.MAIN;
            sendPhotoMessage("main");
            String text = loadMessage("main");
            sendTextMessage(text);
            showMainMenu("Главное меню бота", "/start", "Генерация Tinder-профля", "/profile", "Сообщение для знакомства", "/opener", "Переписка от вашего имени", "/message", "Переписка со звездами", "/date", "Общение с GPT", "/gpt");
            return;
        }
        // command GPT
        if (message.equals("/gpt")) {
            currentMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            String text = loadMessage("gpt");
            sendTextMessage(text);
            return;
        }
        if (currentMode == DialogMode.GPT && !isMessageCommand()) {
            String prompt = loadPrompt("gpt");
            Message msg = sendHtmlMessage(waitText);
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

        if (currentMode == DialogMode.DATE && !isMessageCommand()) {
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
        if (currentMode == DialogMode.MESSAGE && !isMessageCommand()) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("message_")) {
                String prompt = loadPrompt(query);
                String userChatHistory = String.join("\n\n", list);
                Message msg = sendHtmlMessage(waitText);
                String answer = chatGPT.sendMessage(prompt, userChatHistory);
                updateTextMessage(msg, answer);
                return;
            }
            list.add(message);
            return;
        }

        // command PROFILE
        if (message.equals("/profile")) {
            currentMode = DialogMode.PROFILE;
            sendPhotoMessage("profile");

            me = new UserInfo();
            questionCount = 1;
            sendTextMessage("Сколько Вам лет?");
            return;
        }
        if (currentMode == DialogMode.PROFILE && !isMessageCommand()) {
            switch (questionCount) {
                case 1:
                    me.age = message;
                    questionCount++;
                    sendTextMessage("Кем Вы работаете?");
                    return;
                case 2:
                    me.occupation = message;
                    questionCount++;
                    sendTextMessage("У Вас есть хобби?");
                    return;
                case 3:
                    me.hobby = message;
                    questionCount++;
                    sendTextMessage("Что Вам НЕ нравится в людях?");
                    return;
                case 4:
                    me.annoys = message;
                    questionCount++;
                    sendTextMessage("Цель знакомства?");
                    return;
                case 5:
                    me.goals = message;
                    String aboutMyself = me.toString();
                    String prompt = loadPrompt("profile");
                    Message msg = sendHtmlMessage(waitText);
                    String answer = chatGPT.sendMessage(prompt, aboutMyself);
                    updateTextMessage(msg, answer);
                    return;
            }
            return;
        }
        // command OPENER
        if (message.equals("/opener")) {
            currentMode = DialogMode.OPENER;
            sendPhotoMessage("opener");

            she = new UserInfo();
            questionCount = 1;
            sendTextMessage("Как её зовут?");
            return;
        }
        if (currentMode == DialogMode.OPENER && !isMessageCommand()) {
            switch (questionCount) {
                case 1:
                    she.name = message;
                    questionCount++;
                    sendTextMessage("Сколько ей лет?");
                    return;
                case 2:
                    she.age = message;
                    questionCount++;
                    sendTextMessage("Есть ли у неё хобби и какие?");
                    return;
                case 3:
                    she.hobby = message;
                    questionCount++;
                    sendTextMessage("Кем она работает?");
                    return;
                case 4:
                    she.occupation = message;
                    questionCount++;
                    sendTextMessage("Цель знакомства?");
                    return;
                case 5:
                    she.goals = message;
                    String aboutHer = she.toString();
                    String prompt = loadPrompt("opener");
                    Message msg = sendHtmlMessage(waitText);
                    String answer = chatGPT.sendMessage(prompt, aboutHer);
                    updateTextMessage(msg, answer);
                    return;
            }
            return;
        }
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
