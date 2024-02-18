package dmitryv.lab1.requests;

import lombok.Data;

import javax.validation.constraints.Email;

@Data
public class UserRequest {
    @Email
    private String email;
    private String password;
    private String name;
    private boolean isModerator;
}
