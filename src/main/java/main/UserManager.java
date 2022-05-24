package main;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserManager {
	@Getter
	private final Map<Long, User> users = new ConcurrentHashMap<>();

	public User getUser(long userId) {
		User user = users.get(userId);
		if (user == null) {
			user = new User(userId);
			users.put(userId, user);
		}
		return user;
	}
}
