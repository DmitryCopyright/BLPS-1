package dmitryv.lab1.services;

import dmitryv.lab1.models.XmlUser;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import dmitryv.lab1.models.User;
import dmitryv.lab1.repos.UserRepo;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service public class UserService {

    private final UserRepo repo;
    private final BCryptPasswordEncoder encoder;

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

    public Optional<User> get(String email) { return Optional.ofNullable(repo.getByEmail(email)); }

    public Optional<User> get(long id) { return Optional.ofNullable(repo.getByUserId(id)); }

    public boolean exist(String email) { return repo.existsByEmail(email); }

    @Transactional public void save(User u) { this.repo.save(u); }
}