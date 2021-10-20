package bedtime.services;

import bedtime.models.User;

public interface UserService {
    User save(User user);

    User findByUsername(String username);

    boolean findByUsernameAndPassword(String username, String password);
}