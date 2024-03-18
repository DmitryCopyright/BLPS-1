package dmitryv.lab1.services;

import dmitryv.lab1.models.Topic;
import dmitryv.lab1.models.TopicUpdate;
import dmitryv.lab1.repos.TopicRepo;
import dmitryv.lab1.repos.TopicUpdateRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class TopicService {

    @Autowired
    private TopicRepo topicRepo;

    @Autowired
    private TopicUpdateRepo topicUpdateRepository;

    @Autowired
    private TopicUpdateRepo topicUpdateRepo;

    public List<Topic> getAllTopics() {
        return StreamSupport.stream(topicRepo.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    public List<TopicUpdate> getRecentTopicUpdates() {
        LocalDateTime tenSecondsAgo = LocalDateTime.now().minusSeconds(10);
        return topicUpdateRepo.findUpdatesSince(tenSecondsAgo);
    }

    public void checkTopicUpdates() {
        List<Topic> topics = getAllTopics();
        for (Topic topic : topics) {

            boolean hasUpdates = checkForUpdates(topic);
            if (hasUpdates) {
                createTopicUpdateRecord(topic);
            }
        }
    }

    private boolean checkForUpdates(Topic topic) {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        List<TopicUpdate> updates = topicUpdateRepo.findUpdatesInLast24Hours(yesterday);
        return updates.stream().anyMatch(update -> update.getTopic().equals(topic));
    }

    public void createTopicUpdateRecord(Topic topic) {
        TopicUpdate update = new TopicUpdate();
        update.setTopic(topic);
        update.setUpdatedAt(LocalDateTime.now());
        topicUpdateRepository.save(update);
    }
}
