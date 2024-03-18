package dmitryv.lab1.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data @Entity @Table(name = "messages")
public class Message {

    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "message_seq_gen")
    @SequenceGenerator(name = "users_seq_gen", sequenceName = "message_id_seq")
    @Column(name = "message_id")
    @Id private long messageId;
    @ManyToOne(optional = false, cascade = CascadeType.ALL) @JoinColumn(name = "user_id") private User user;
    private String text_message;
    @Nullable private String name;
    @Column(name = "published_date")
    private LocalDateTime publishedDate;
    @ManyToOne
    @JoinColumn(name = "topic_id")
    @JsonBackReference
    private Topic topic;

    public Message() {}

    public Message(User u, String text_message, String name, LocalDateTime publishedDate) {
        this.user = u;
        this.text_message = text_message;
        this.name = name;
        this.publishedDate = publishedDate;
    }

    public String getTextMessage() {
        return text_message;
    }
    public void setTextMessage(String text_message) {
        this.text_message = text_message;
    }

    public long getMessageId() {
        return messageId;
    }


}
