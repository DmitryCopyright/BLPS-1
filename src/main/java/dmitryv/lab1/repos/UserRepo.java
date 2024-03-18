package dmitryv.lab1.repos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import dmitryv.lab1.models.User;

import java.util.Optional;

@Repository public interface UserRepo extends CrudRepository<User, Long> {

    Optional<User> findByEmail(String email);

    User getByUserId(long id);

    boolean existsByEmail(String email);
}