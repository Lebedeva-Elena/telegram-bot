package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final Pattern MESSAGE_PATTERN = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");
    private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final TelegramBot telegramBot;
    private final NotificationTaskRepository repository;

    public NotificationServiceImpl(TelegramBot telegramBot, NotificationTaskRepository repository) {
        this.telegramBot = telegramBot;
        this.repository = repository;
    }


    @Override
    public void process(Update update) {
        System.out.println(update);
        Long chatId = update.message().chat().id();
        String text = update.message().text();
        if (text.equals("/start")) {
            telegramBot.execute(new SendMessage(chatId, "Привет, мой забывчивый гость. " +
                    "Расскажи о своих предстоящих событиях и я позабочусь о том, чтобы ты не о них не забыл"));
            return;
        }
        Matcher matcher = MESSAGE_PATTERN.matcher(text);
        if (matcher.find()) {
            String dateWithNotification = matcher.group(1);
            LocalDateTime alarmDate = LocalDateTime.parse(dateWithNotification, DATE_TIME_FORMATTER);
            String notification = matcher.group(3);
            repository.save(new NotificationTask(chatId, notification, alarmDate));
            telegramBot.execute(new SendMessage(chatId, "Напоминание сохранено, теперь ты можешь на меня положиться"));
        } else {
            telegramBot.execute(new SendMessage(chatId, "Напиши дату в формате dd.MM.yyyy HH:mm и какое мероприятие тебя  ожидает"));

        }

    }



}


