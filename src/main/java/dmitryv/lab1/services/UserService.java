package dmitryv.lab1.services;

import dmitryv.lab1.models.Topic;
import dmitryv.lab1.models.XmlUser;
import dmitryv.lab1.repos.TopicRepo;
import lombok.val;
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

    @Transactional
    public Optional<User> add(String email, String password, String name, boolean isModerator) {
        String encodedPassword = encoder.encode(password);
        val u = new User(email, encodedPassword, name, isModerator);
        this.save(u);
        Optional<User> registeredUser = this.get(email);

        // После добавления в БД сохраняем пользователя в XML
        registeredUser.ifPresent(user -> xmlUserDetailsService.saveUser(
                new XmlUser(user.getEmail(), user.getPassword(), user.isModerator() ? "MODERATOR" : "USER")
        ));

        return registeredUser;
    }

    public List<User> getAll() { return StreamSupport.stream(repo.findAll().spliterator(), false).collect(Collectors.toList()); }

    public Optional<User> get(String email) { return repo.findByEmail(email); }

    public Optional<User> get(long id) { return Optional.ofNullable(repo.getByUserId(id)); }

    public boolean exist(String email) { return repo.existsByEmail(email); }

    @Transactional public void save(User u) { this.repo.save(u); }

    public boolean subscribeUserToTopic(String email, Long topicId) {
        try {
            User user = repo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            Topic topic = topicRepo.findById(topicId).orElseThrow(() -> new EntityNotFoundException("Topic not found"));

            if (!user.getTopics().contains(topic)) {
                user.getTopics().add(topic);
                repo.save(user);
            }
            return true;
        } catch (Exception e) {

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