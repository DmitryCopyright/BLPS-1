package dmitryv.lab1.services;

import dmitryv.lab1.models.Notification;
import dmitryv.lab1.models.Topic;
import dmitryv.lab1.models.TopicUpdate;
import dmitryv.lab1.models.User;
import dmitryv.lab1.repos.NotificationRepo;
import dmitryv.lab1.repos.TopicRepo;
import dmitryv.lab1.repos.TopicUpdateRepo;
import dmitryv.lab1.repos.UserRepo;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private TopicUpdateRepo topicUpdateRepository;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private TopicRepo topicRepo;

    @Autowired
    private NotificationRepo notificationRepo;

    public List<String> getNotificationsForUser(String email) {
        return notificationRepo.findByUserEmail(email).stream()
                .map(Notification::getMessage)
                .collect(Collectors.toList());
    }

    public void processReceivedMessage(String message, String topicName) {
        // Поиск топика по имени
        Optional<Topic> topicOptional = topicRepo.findByName(topicName);
        if (!topicOptional.isPresent()) {
            Hibernate.initialize(topicOptional.get().getSubscribers());
        };

        Topic topic = topicOptional.get();

        // Создание записи об обновлении для топика
        TopicUpdate topicUpdate = new TopicUpdate();
        topicUpdate.setTopic(topic);
        topicUpdateRepository.save(topicUpdate);
    }

    public List<TopicUpdate> findUpdatesInLast24Hours() {
        LocalDateTime now = LocalDateTime.now();
        return topicUpdateRepository.findUpdatesInLast24Hours(now.minusDays(1));
    }


    @Transactional
    public void generateInstantNotification(Topic topic, User excludedUser) {
        try {
            String notificationMessage = "Topic '" + topic.getName() + "' has been updated.";
            topic.getSubscribers().forEach(subscriber -> {
                if (!subscriber.equals(excludedUser)) { // Исключаем автора изменения
                    Notification notification = new Notification();
                    notification.setUser(subscriber);
                    notification.setMessage(notificationMessage);
                    Notification savedNotification = notificationRepo.save(notification);
                }
            });
        } catch (Exception e) {
            logger.error("Ошибка при генерации уведомления: ", e);
        }
    }
}