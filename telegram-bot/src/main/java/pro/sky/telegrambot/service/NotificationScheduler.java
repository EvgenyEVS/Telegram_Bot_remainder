package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class NotificationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);

    @Autowired
    private TelegramBot telegramBot;
    @Autowired
    private NotificationTaskRepository notificationTaskRepository;

    @Scheduled(fixedDelay = 60000)
    public void checkAndSendReminders() {
        LocalDateTime now = LocalDateTime.now();

        List<NotificationTask> tasks = notificationTaskRepository.findByDateTimeBefore(now);

        for (NotificationTask task : tasks) {
            try {
                String message = "Напоминание! \n" + task.getRemindText();
                SendMessage sendMessage = new SendMessage(task.getChatId(), message);
                telegramBot.execute(sendMessage);

                notificationTaskRepository.delete(task);
            } catch (Exception e) {
                logger.info(e.getMessage());
                e.printStackTrace();
            }
        }
    }

}
