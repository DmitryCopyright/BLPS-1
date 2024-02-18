package dmitryv.lab1.requests;

import lombok.Data;

import java.io.Serializable;

@Data
public class MessageEditReq implements Serializable {

    private static final long serialVersionUID = 4L;

    private long messageId;
    private String newText;

   }