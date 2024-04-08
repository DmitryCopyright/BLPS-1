package dmitryv.lab1.services;

import dmitryv.lab1.delegators.RegisterUserDelegate;
import dmitryv.lab1.models.Topic;
import dmitryv.lab1.models.XmlUser;
import dmitryv.lab1.repos.TopicRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import dmitryv.lab1.models.User;
import dmitryv.lab1.repos.UserRepo;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service public class UserService {

    private final UserRepo repo;
    private final BCryptPasswordEncoder encoder;

    @Autowired
    private TopicRepo topicRepo;

    @Autowired
    private XmlUserDetailsService xmlUserDetailsService;

    @Autowired public UserService(UserRepo userRepo , BCryptPasswordEncoder encoder) {
        this.repo = userRepo;
        this.encoder = encoder;
}

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterUserDelegate.class);



    public Optional<User> add(String email, String password, String name, boolean isModerator) {
        LOGGER.info("Attempting to add a new user with email: {}", email);
        if (this.repo.existsByEmail(email)) {
            LOGGER.warn("User with email {} already exists", email);
            return Optional.empty();
        }

        String encodedPassword = encoder.encode(password);
        User user = new User(email, encodedPassword, name, isModerator);
        LOGGER.info("Created user entity: {}", user);

        try {
            this.repo.save(user);
            LOGGER.info("User with email: {} saved successfully.", email);
            XmlUser xmlUser = new XmlUser(email, encodedPassword, isModerator ? "MODERATOR" : "USER");
            xmlUserDetailsService.saveUser(xmlUser);
            LOGGER.info("User with email: {} saved to XML as well.", email);
            return Optional.of(user);
        } catch (Exception e) {
            LOGGER.error("Exception occurred while saving user with email: {}", email, e);
            return Optional.empty();
        }
    }


    public List<User> getAll() { return StreamSupport.stream(repo.findAll().spliterator(), false).collect(Collectors.toList()); }

    public Optional<User> get(String email) { return repo.findByEmail(email); }

    public Optional<User> get(long id) { return Optional.ofNullable(repo.getByUserId(id)); }

    public boolean exist(String email) { return repo.existsByEmail(email); }


    public void save(User u) {
        LOGGER.info("Saving user with email: {}", u.getEmail());
        try {
            this.repo.save(u);
            LOGGER.info("User with email: {} saved to the database.", u.getEmail());
        } catch (Exception e) {
            LOGGER.error("Error saving user with email: {}", u.getEmail(), e);
            throw e;
        }
    }

    @Transactional
    public boolean subscribeUserToTopic(String email, Long topicId) {
        try {
            User user = repo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
            Topic topic = topicRepo.findById(topicId).orElseThrow(() -> new EntityNotFoundException("Topic not found: " + topicId));

            if (!user.getTopics().contains(topic)) {
                user.getTopics().add(topic);
                repo.save(user);
                LOGGER.info("User {} subscribed to topic {}", email, topicId);
                return true;
            } else {
                LOGGER.info("User {} already subscribed to topic {}", email, topicId);
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("Error subscribing user {} to topic {}: ", email, topicId, e);
            return false;
        }
    }
    public void unsubscribeUserFromTopic(String email, Long topicId) {
        User user = repo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Topic topic = topicRepo.findById(topicId).orElseThrow(() -> new EntityNotFoundException("Topic not found"));
        user.getTopics().remove(topic);
        repo.save(user);
    }
}