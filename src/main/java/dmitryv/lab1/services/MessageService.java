package dmitryv.lab1.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import dmitryv.lab1.models.Message;
import dmitryv.lab1.models.User;
import dmitryv.lab1.repos.MessageRepo;
import dmitryv.lab1.requests.MessageReqFilters;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service public class MessageService {

    private final MessageRepo repo;
    private static final Logger log = LoggerFactory.getLogger(MessageService.class);


    @Autowired public MessageService(MessageRepo repo) {
        this.repo = repo;
    }

    public boolean add(Message a) {
        if (a.getUser() == null || !ModeratorService.moderate(a)) return false;
        else {
            this.save(a);
            return true;
        }
    }

    public List<Message> getAll() {
        return StreamSupport.stream(repo.findAll().spliterator(), false).collect(Collectors.toList());
    }

    public Message get(long id) { return repo.getByMessageId(id); }

    @Transactional public void save(Message a) { this.repo.save(a); }

    @Transactional
    public void deleteMessage(Long messageId) {
        repo.findById(messageId).ifPresent(message -> {
            // Отсоединяем сообщение от пользователя, чтобы избежать каскадного удаления
            User user = message.getUser();
            if (user != null) {
                user.getMessages().remove(message);
                message.setUser(null); // Отсоединяем сообщение от пользователя
                repo.save(message); // Сохраняем изменения перед удалением
            }
            log.info("Attempting to delete message with ID: {}", message.getMessageId());
            repo.delete(message);
            log.info("Message with ID: {} deleted successfully", message.getMessageId());
        });
    }

    public List<Message> getFilteredMessages(MessageReqFilters req, Optional<Long> userId) {
        Stream<Message> filteredMessages = StreamSupport.stream(repo.findAll().spliterator(), false);

        // Фильтрация по userId, если указан и если запрос не от модератора
        if (userId.isPresent()) {
            filteredMessages = filteredMessages.filter(m -> m.getUser().getUserId() == userId.get());
        }

        // Фильтрация по тексту сообщения, если указан
        if (req.text_message != null && !req.text_message.isEmpty()) {
            filteredMessages = filteredMessages.filter(m -> m.getTextMessage().contains(req.text_message));
        }

        // Фильтрация по дате публикации, если указаны startDate и endDate
        if (req.startDate != null && req.endDate != null) {
            filteredMessages = filteredMessages.filter(m ->
                    m.getPublishedDate() != null &&
                            !m.getPublishedDate().isBefore(req.startDate) &&
                            !m.getPublishedDate().isAfter(req.endDate));
        }

        return filteredMessages.collect(Collectors.toList());
    }

    @Transactional
    public boolean editMessage(long userId, long messageId, String newText) {
        Message message = repo.getByMessageId(messageId);
        if (message != null && message.getUser().getUserId() == userId) {
            message.setTextMessage(newText);
            repo.save(message);
            return true;
        }
        return false;
    }

    public List<Message> getMessagesByUserId(long userId) {
        return StreamSupport.stream(repo.findAll().spliterator(), false)
                .filter(message -> message.getUser().getUserId() == userId)
                .collect(Collectors.toList());
    }
}
