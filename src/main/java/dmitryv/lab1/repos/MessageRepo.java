package dmitryv.lab1.repos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import dmitryv.lab1.models.Message;

@Repository public interface MessageRepo extends CrudRepository<Message, Long> {

    Message getByMessageId(long id);
}