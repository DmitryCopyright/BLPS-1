package dmitryv.lab1.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import dmitryv.lab1.serializers.UserSerializer;
import javax.persistence.*;
import javax.validation.constraints.Email;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data @Entity @Table(name = "users") @JsonSerialize(using = UserSerializer.class)
public class User implements Serializable, UserDetails {

    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_seq_gen")
    @SequenceGenerator(name = "users_seq_gen", sequenceName = "users_id_seq")
    @Id private long userId;
    @Transient private static final long serialVersionUID = 4L;
    @Nullable @Email private String email;
    @Nullable private String password;
    @Nullable private String name;
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.PERSIST) private List<Message> messages;
    private boolean isModerator;

    public User() {}

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public User(String email, String password, String name, boolean isModerator) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.isModerator = isModerator;
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("User"));
        if (isModerator) {
            authorities.add(new SimpleGrantedAuthority("Moderator"));
        }
        return authorities;
    }
    @Override public String getPassword() { return password; }

    @Override public String getUsername() { return email; }

    @Override public boolean isAccountNonExpired() { return true; }

    @Override public boolean isAccountNonLocked() { return true; }

    @Override public boolean isCredentialsNonExpired() { return true; }

    @Override public boolean isEnabled() { return true; }
}