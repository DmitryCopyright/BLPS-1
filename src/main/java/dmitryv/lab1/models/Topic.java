package dmitryv.lab1.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "topics")
public class Topic {
    public Long getId() {
        return id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "topic_id")
    private Long id;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private String name;

    public Set<User> getSubscribers() {
        return subscribers;
    }

    @ManyToMany(mappedBy = "topics", fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    private Set<User> subscribers = new HashSet<>();

    public List<Message> getMessages() {
        return messages;
    }

    @OneToMany(mappedBy = "topic")
    @JsonManagedReference
    private List<Message> messages = new ArrayList<>();

}
