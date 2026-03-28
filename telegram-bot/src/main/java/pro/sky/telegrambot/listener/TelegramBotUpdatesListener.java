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
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;
    @Autowired
    private NotificationTaskRepository notificationTaskRepository;

    private final Pattern pattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)");

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

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
                            handleRemainderMessage(chatID, messageText);
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
                "\n Введите '/help' для получения списка команд." +
                "\n\n Для создания напоминания отправьте сообщение в формате: " +
                "\n 01.01.2001 12:00 Записаться на стрижку";
        telegramBot.execute(new SendMessage(chatID, response));
    }

    private void handleHelpCommand(Long chatID) {
        String response = "Доступные команды:\n" +
                "/start - начать работу \n" +
                "/help - показать это сообщение." +
                "\n\n Для создания напоминания отправьте сообщение в формате: " +
                "\n 01.01.2001 12:00 Записаться на стрижку";
        telegramBot.execute(new SendMessage(chatID, response));
    }

    private void handleUnknownCommand(Long chatID) {
        String response = "Неизвестная команда \n" +
                "Unknown command." +
                "\n\n Для создания напоминания отправьте сообщение в формате: " +
                "\n 01.01.2001 12:00 Записаться на стрижку";
        telegramBot.execute(new SendMessage(chatID, response));
    }

    private void handleRemainderMessage(Long chatId, String text) {

        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String dateTime = matcher.group(1);
            String remainder = matcher.group(3);

            try {
                LocalDateTime localDateTime = LocalDateTime.parse(dateTime, formatter);
                if (localDateTime.isBefore(LocalDateTime.now())) {
                    telegramBot.execute(new SendMessage(chatId, "Некорректная дата"));
                    return;
                }

                NotificationTask notificationTask = new NotificationTask();
                notificationTask.setChatId(chatId);
                notificationTask.setRemindText(remainder);
                notificationTask.setDateTime(localDateTime);

                notificationTaskRepository.save(notificationTask);


                String response = String.format(
                        "Напоминание создано! \n" +
                                "Дата: %s\n" +
                                "Текст: %s\n",
                        dateTime, remainder
                );

                telegramBot.execute(new SendMessage(chatId, response));
            } catch (Exception e) {
                telegramBot.execute(new SendMessage(
                        chatId, "Ошибка. Используйте формат: '01.01.2001 12:00 Записаться на стрижку' "));
            }


        } else handleUnknownCommand(chatId);
    }
}
