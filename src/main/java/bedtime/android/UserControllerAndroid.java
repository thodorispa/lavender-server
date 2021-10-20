package bedtime.android;

import bedtime.utils.CustomResponse;
import bedtime.models.User;
import bedtime.services.SecurityService;
import bedtime.services.UserService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController @Slf4j
public class UserControllerAndroid {
    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    @Value("${jwt.secret}")
    private String secret;


    @PostMapping(value = "/android-login", produces = "application/json", consumes = "application/json")
    public CustomResponse login(@RequestBody User user, HttpServletRequest request) {
        Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());

        boolean found = false;
        if (user.getUsername() != null || user.getPassword() != null) {
            found = userService.findByUsernameAndPassword(user.getUsername(), user.getPassword());
        }

        if (found) {
            String jwtToken = JWT.create()
                    .withSubject(user.getUsername())
                    .withIssuer(request.getRequestURL().toString())
                    .sign(algorithm);
            log.error(jwtToken);

            return new CustomResponse(200, user, jwtToken, "Success!");
        } else {
            return new CustomResponse(403, null, "", "Authorization Failed");
        }
    }

    @PostMapping(value = "/registration", produces = "application/json", consumes = "application/json")
    public CustomResponse registration(@RequestBody User user, HttpServletRequest request) {
        Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());
        User newUser = userService.save(user);

        if (newUser != null) {
            String jwtToken = JWT.create()
                    .withSubject(user.getUsername())
                    .withIssuer(request.getRequestURL().toString())
                    .sign(algorithm);

            securityService.autoLogin(user.getUsername(), user.getPasswordConfirm());

            return new CustomResponse(200, user, jwtToken, "Success!");
        }

        return new CustomResponse(404, null, "","Username already exists..");
    }
}