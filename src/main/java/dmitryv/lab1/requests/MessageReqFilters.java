package dmitryv.lab1.requests;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data @Builder @Value
public class MessageReqFilters implements Serializable {

    public static final long serialVersionUID = 4L;
    public int[] messageIds;
    public long userId;
    public String text_message;
    public String name;
    public LocalDateTime startDate;
    public LocalDateTime endDate;

    // Constructor accepting the correct arguments
    public MessageReqFilters(long userId, String text_message, LocalDateTime startDate, LocalDateTime endDate) {
        this.userId = userId;
        this.text_message = text_message;
        this.startDate = startDate;
        this.endDate = endDate;
        this.messageIds = new int[0];
        this.name = "";
    }

    // Constructor accepting all arguments
    public MessageReqFilters(int[] messageIds, long userId, String text_message, String name, LocalDateTime startDate, LocalDateTime endDate) {
        this.messageIds = messageIds;
        this.userId = userId;
        this.text_message = text_message;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}