package dmitryv.lab1.services;

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

    @Autowired public UserService(UserRepo userRepo , BCryptPasswordEncoder encoder) {
        this.repo = userRepo;
        this.encoder = encoder;
    }

    @Transactional public Optional<User> add(String email, String password, String name, boolean isModerator) {
        val u = new User(email, encoder.encode(password), name, isModerator);
        this.save(u);
        return this.get(email); //загружаем из базы данных, чтобы получить сущность уже с присвоенным id
    }

    public List<User> getAll() { return StreamSupport.stream(repo.findAll().spliterator(), false).collect(Collectors.toList()); }

    public Optional<User> get(String email) { return Optional.ofNullable(repo.getByEmail(email)); }

    public Optional<User> get(long id) { return Optional.ofNullable(repo.getByUserId(id)); }

    public boolean exist(String email) { return repo.existsByEmail(email); }

    @Transactional public void save(User u) { this.repo.save(u); }
}