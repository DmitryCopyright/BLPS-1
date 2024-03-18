package dmitryv.lab1.repos;

import dmitryv.lab1.models.Topic;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import dmitryv.lab1.models.Message;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository public interface MessageRepo extends CrudRepository<Message, Long> {

    Message getByMessageId(long id);

    @Query("SELECT m FROM Message m WHERE m.topic = :topic ORDER BY m.publishedDate DESC")
    List<Message> findTopByTopicOrderByIdDesc(@Param("topic") Topic topic, Pageable pageable);
}