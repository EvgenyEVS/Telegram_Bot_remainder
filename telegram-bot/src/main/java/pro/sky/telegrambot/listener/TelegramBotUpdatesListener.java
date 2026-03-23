package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            if (update.message() != null) {
                Message message = update.message();
                String messageText = message.text();
                Long chatID = message.chat().id();

                if (messageText != null) {
                    switch (messageText) {
                        case "/start":
                            handleStartCommand(chatID, message.chat().firstName());
                            break;

                        case "/help":
                            handleHelpCommand(chatID);
                            break;

                        default:
                            handleUnknownCommand(chatID);
                            break;
                    }
                }
            }
            logger.info("Processing update: {}", update);
            // Process your updates here
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }


    private void handleStartCommand(Long chatID, String userName) {
        String response = "Привет, " + userName + "!" +
                "\n Я готов к работе. " +
                "\n Введите '/help' для получения списка команд.";
        telegramBot.execute(new SendMessage(chatID, response));
    }

    private void handleHelpCommand(Long chatID) {
        String response = "Доступные команды:\n" +
                "/start - начать работу \n" +
                "/help - показать это сообщение.";
        telegramBot.execute(new SendMessage(chatID, response));
    }

    private void handleUnknownCommand(Long chatID) {
        String response = "Неизвестная команда \n" +
                "Unknown command.";
        telegramBot.execute(new SendMessage(chatID, response));
    }
}
