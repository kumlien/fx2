package hoggaster.user;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class User {
	
	@Id
	private String id;
	
	private final UserAccount account;

	public User(UserAccount account) {
		this.account = account;
	}

	@PersistenceConstructor
	User(String id, UserAccount account) {
		this.id = id;
		this.account = account;
	}

	public UserAccount getAccount() {
		return account;
	}
}
