package bedtime.services;

import bedtime.models.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import bedtime.repository.RoleRepository;
import bedtime.repository.UserRepository;

import java.util.Collections;
import java.util.HashSet;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public User save(User user) {
        //Check if already exists
        if (userRepository.findByUsername(user.getUsername())!=null) {
            System.out.println("Error creating user.");
            return null;
        }else {
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            user.setRoles(new HashSet<>(Collections.singletonList(roleRepository.findByName("ROLE_USER"))));
           return userRepository.save(user);
        }
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public boolean findByUsernameAndPassword(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user!=null && bCryptPasswordEncoder.matches(password, user.getPassword())){
            return true;
        } else return false;
    }
}