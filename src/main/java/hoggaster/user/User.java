package hoggaster.user;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
public class User {

    @Id
    private String id;

    private final String firstName;

    private final String lastName;

    private final String email;

    private final String password;

    @DBRef(lazy = true)
    private List<Depot> depots;


    @PersistenceConstructor
    public User(String firstName, String lastName, String email, String password, String id) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.id = id;
    }


    public User(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }


    public List<Depot> getDepots() {
        return depots;
    }


    public void addDepot(Depot depot) {
        depots.add(depot);
    }
}
