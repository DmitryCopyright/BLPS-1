package dmitryv.lab1.services;

import dmitryv.lab1.models.Message;
import dmitryv.lab1.models.TopicUpdate;
import dmitryv.lab1.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;

@Component
@EnableScheduling
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Scheduled(fixedRate = 10000)
    public void checkForUpdates() {
        logger.info("Планировщик запущен: проверка на наличие обновлений");

        List<TopicUpdate> recentUpdates = topicService.getRecentTopicUpdates();
        if (recentUpdates.isEmpty()) {
            logger.info("Новые обновления не найдены.");
        } else {
            logger.info("Обновления найдены", recentUpdates.size());
            recentUpdates.forEach(update -> {

                Optional<Message> lastMessageOptional = messageService.getLastMessageForTopic(update.getTopic());

                lastMessageOptional.ifPresent(lastMessage -> {

                    User author = lastMessage.getUser();

                    transactionTemplate.execute(status -> {
                        try {
                            // Сгенерировать уведомления для всех подписчиков топика, исключая автора
                            notificationService.generateInstantNotification(update.getTopic(), author);
                        } catch (Exception e) {
                            logger.error("Ошибка при генерации уведомлений: ", e);
                            status.setRollbackOnly();
                        }
                        return null;
                    });
                });
            });
        }

        logger.info("Планировщик: проверка завершена.");
    }
}
