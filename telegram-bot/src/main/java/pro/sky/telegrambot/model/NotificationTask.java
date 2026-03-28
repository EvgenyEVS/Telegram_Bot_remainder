package pro.sky.telegrambot.model;


import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "tasks_for_remind")

public class NotificationTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "remind_text")
    private String remindText;

    @Column(name = "date_time")
    private LocalDateTime dateTime;

    public NotificationTask(Long chatId, String remindText, LocalDateTime dateTime) {
        this.chatId = chatId;
        this.remindText = remindText;
        this.dateTime = dateTime;
    }
}
