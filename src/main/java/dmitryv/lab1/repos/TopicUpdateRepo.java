package dmitryv.lab1.repos;

import dmitryv.lab1.models.Topic;
import dmitryv.lab1.models.TopicUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TopicUpdateRepo extends JpaRepository<TopicUpdate, Long> {
    List<TopicUpdate> findByTopicId(Long topicId);

    List<TopicUpdate> findByTopic(Topic topic);

    @Query("SELECT tu FROM TopicUpdate tu WHERE tu.updatedAt > :date")
    List<TopicUpdate> findUpdatesInLast24Hours(@Param("date") LocalDateTime date);

    @Query("SELECT tu FROM TopicUpdate tu WHERE tu.updatedAt > :dateTime")
    List<TopicUpdate> findUpdatesSince(@Param("dateTime") LocalDateTime dateTime);
}
