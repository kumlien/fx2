package hoggaster.domain.users;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Document
public class User extends org.springframework.security.core.userdetails.User {

    @Id
    private String id;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String firstName;

    public String lastName;

    public String email;


    @PersistenceConstructor
    public User(String username, String firstName, String lastName, String email, String password, String id, Collection<GrantedAuthority> authorities) {
        this(username,firstName, lastName, email, password, authorities);
        this.id = id;
    }


    public User(String username, String firstName, String lastName, String email, String password, Collection<GrantedAuthority> authorities) {
        super(username,password,true, true, true, true, authorities);
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String getId() {
        return id;
    }
}
