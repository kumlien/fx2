package hoggaster.user;

import java.io.Serializable;
import java.util.Set;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Info for a user account. One-to-one relation to a {@link User} 
 * Probably embedded in the {@link User} {@link Document}
 */
@SuppressWarnings("serial")
@Document
public class UserAccount implements Serializable {
	
	@DBRef
	private final Set<Depot> depots;

	public UserAccount(Set<Depot> depots) {
		this.depots = depots;
	}

	public Set<Depot> getDepots() {
		return depots;
	}
}
