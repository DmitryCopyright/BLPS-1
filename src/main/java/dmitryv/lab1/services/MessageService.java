package dmitryv.lab1.services;

import dmitryv.lab1.models.*;
import dmitryv.lab1.repos.NotificationRepo;
import dmitryv.lab1.repos.TopicRepo;
import dmitryv.lab1.repos.TopicUpdateRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import dmitryv.lab1.repos.MessageRepo;
import dmitryv.lab1.requests.MessageReqFilters;

import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service public class MessageService {

    private final MessageRepo repo;
    private final ModeratorService moderatorService;
    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private TopicRepo topicRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private TopicUpdateRepo topicUpdateRepo;

    @Autowired
    private NotificationRepo notificationRepo;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MessageRepo messageRepo;


    @Autowired public MessageService(MessageRepo repo, ModeratorService moderatorService) {
        this.repo = repo;
        this.moderatorService = moderatorService;
    }

    @Transactional
    public boolean add(Message message, User user) {
        if (message.getUser() == null || !moderatorService.moderate(message, user)) return false;
        else {
            this.save(message);
            return true;
        }
    }

    public Optional<Message> addMessage(Message message, User user, String topicName) {
        Topic topic = topicRepo.findByName(topicName).orElseGet(() -> {
            Topic newTopic = new Topic();
            newTopic.setName(topicName);
            topicRepo.save(newTopic);
            return newTopic;
        });

        message.setTopic(topic);
        message.setUser(user);
        // Установка времени публикации и имени пользователя
        message.setPublishedDate(LocalDateTime.now());
        message.setName(user.getName());

        Message savedMessage = repo.save(message);

        userService.subscribeUserToTopic(user.getEmail(), topic.getId());

        rabbitTemplate.convertAndSend("forumTopicExchange", "topic.newMessage", "New message in topic: " + topicName);


        sendMessageToTopic(topicName, "New message in topic: " + topicName);

        createTopicUpdate(topic);

        if (!topic.getSubscribers().isEmpty()) {
            Set<User> subscribers = topic.getSubscribers();

            String notificationMessage = "In topic '" + topicName + "' new message from " + user.getName() + ".";

            subscribers.forEach(subscriber -> {
                if (!subscriber.equals(user)) { // Проверка, что подписчик не является автором сообщения
                    Notification notification = new Notification();
                    notification.setUser(subscriber);
                    notification.setMessage(notificationMessage);
                    notificationRepo.save(notification);
                }
            });
        }

        return Optional.ofNullable(savedMessage);
    }

    private void createTopicUpdate(Topic topic) {
        TopicUpdate topicUpdate = new TopicUpdate();
        topicUpdate.setTopic(topic);
        topicUpdate.setUpdatedAt(LocalDateTime.now());
        topicUpdateRepo.save(topicUpdate);
    }

    public List<Message> getAll() {
        return StreamSupport.stream(repo.findAll().spliterator(), false).collect(Collectors.toList());
    }

    public Message get(long id) { return repo.getByMessageId(id); }

    @Transactional public void save(Message a) { this.repo.save(a); }

    @Transactional
    public void deleteMessage(Long messageId) {
        repo.findById(messageId).ifPresent(message -> {
            Topic topic = message.getTopic();
            User user = message.getUser();
            if (user != null) {
                user.getMessages().remove(message);
                message.setUser(null);
                repo.save(message);
            }
            log.info("Attempting to delete message with ID: {}", message.getMessageId());
            repo.delete(message);
            log.info("Message with ID: {} deleted successfully", message.getMessageId());

            // Отправка сообщения в RabbitMQ о удалении сообщения
            rabbitTemplate.convertAndSend("forumTopicExchange", "topic.messageDeleted", "Message deleted in topic: " + topic.getName());

            // Создание записи об обновлении топика
            createTopicUpdate(topic);


            String notificationMessage = "Message deleted in topic '" + topic.getName() + "'.";
            topic.getSubscribers().forEach(subscriber -> {
                if (!subscriber.equals(user)) {
                    Notification notification = new Notification();
                    notification.setUser(subscriber);
                    notification.setMessage(notificationMessage);
                    notificationRepo.save(notification);
                }
            });

            // Отправка уведомления через STOMP
            sendMessageToTopic(topic.getName(), notificationMessage);
        });
    }

    public List<Message> getFilteredMessages(MessageReqFilters req, Optional<Long> userId) {
        Stream<Message> filteredMessages = StreamSupport.stream(repo.findAll().spliterator(), false);


        if (userId.isPresent()) {
            filteredMessages = filteredMessages.filter(m -> m.getUser().getUserId() == userId.get());
        }


        if (req.text_message != null && !req.text_message.isEmpty()) {
            filteredMessages = filteredMessages.filter(m -> m.getTextMessage().contains(req.text_message));
        }


        if (req.startDate != null && req.endDate != null) {
            filteredMessages = filteredMessages.filter(m ->
                    m.getPublishedDate() != null &&
                            !m.getPublishedDate().isBefore(req.startDate) &&
                            !m.getPublishedDate().isAfter(req.endDate));
        }

        return filteredMessages.collect(Collectors.toList());
    }

    @Transactional
    public Optional<Message> editMessage(long userId, long messageId, String newText) {
        Optional<Message> messageOptional = repo.findById(messageId);

        if (messageOptional.isPresent()) {
            Message message = messageOptional.get();

            if (message.getUser().getUserId() == userId) {
                message.setTextMessage(newText);
                Message savedMessage = repo.save(message);

                createTopicUpdate(message.getTopic());

              notificationService.generateInstantNotification(message.getTopic(), message.getUser());

                return Optional.of(savedMessage);
            }
        }
        return Optional.empty();
    }

    public Optional<Message> getLastMessageForTopic(Topic topic) {
        Pageable limit = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "publishedDate"));
        List<Message> messages = messageRepo.findTopByTopicOrderByIdDesc(topic, limit);
        if (!messages.isEmpty()) {
            return Optional.of(messages.get(0));
        }
        return Optional.empty();
    }

    public List<Message> getMessagesByUserId(long userId) {
        return StreamSupport.stream(repo.findAll().spliterator(), false)
                .filter(message -> message.getUser().getUserId() == userId)
                .collect(Collectors.toList());
    }

    public boolean isOwner(String username, Long messageId) {
        return repo.findById(messageId)
                .map(Message::getUser)
                .map(User::getUsername)
                .filter(u -> u.equals(username))
                .isPresent();
    }

    public void sendMessageToTopic(String topic, String message) {
        messagingTemplate.convertAndSend("/topic/" + topic, message);
    }
}
