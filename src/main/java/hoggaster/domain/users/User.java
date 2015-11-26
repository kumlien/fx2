package hoggaster.domain.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class User {

    @Id
    private String id;

    public final String username;

    public final String firstName;

    public final String lastName;

    public final String email;

    @JsonIgnore
    public final String password;


    @PersistenceConstructor
    public User(String username, String firstName, String lastName, String email, String password, String id) {
        this(username,firstName, lastName, email, password);
        this.id = id;
    }


    public User(String username, String firstName, String lastName, String email, String password) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    public String getId() {
        return id;
    }
}
