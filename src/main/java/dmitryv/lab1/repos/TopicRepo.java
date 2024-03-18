package dmitryv.lab1.repos;

import dmitryv.lab1.models.Topic;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TopicRepo extends CrudRepository<Topic, Long> {
    Optional<Topic> findByName(String name);
}
