package dmitryv.lab1.requests;

import lombok.*;
import dmitryv.lab1.models.Message;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data @Builder @Value
public class MessageReq implements Serializable {

    public static final long serialVersionUID = 4L;
    public int[] messageIds;
    public long userId;
    public String text_message;
    public String name;
    private LocalDateTime publishedDate;

    public Message toMessage() {
        return new Message(null, text_message, name, publishedDate);
    }
}