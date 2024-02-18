package dmitryv.lab1.responses;

import lombok.*;
import dmitryv.lab1.models.Message;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Builder @Data @Value
public class MessageRes implements Serializable {

    public static final long serialVersionUID = 4L;
    @Builder.Default public List<Message> messages = Collections.emptyList();
    @Builder.Default public String msg = "";
}
