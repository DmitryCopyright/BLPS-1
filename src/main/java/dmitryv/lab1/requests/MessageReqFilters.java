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

}