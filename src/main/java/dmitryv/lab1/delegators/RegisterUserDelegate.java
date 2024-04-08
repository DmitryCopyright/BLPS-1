package dmitryv.lab1.delegators;


import dmitryv.lab1.models.User;
import dmitryv.lab1.services.UserService;
import dmitryv.lab1.services.XmlUserDetailsService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class RegisterUserDelegate implements JavaDelegate {

    @Autowired
    private UserService userService;
    @Autowired
    private XmlUserDetailsService xmlUserDetailsService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterUserDelegate.class);

    @Override
    public void execute(DelegateExecution execution) {
        LOGGER.info("Delegate executed by thread: {}", Thread.currentThread().getName());
        String email = (String) execution.getVariable("email");
        String password = (String) execution.getVariable("password");
        String name = (String) execution.getVariable("name");
        Boolean isModerator = Boolean.parseBoolean((String) execution.getVariable("isModerator"));

        String encodedPassword = passwordEncoder.encode(password);
        Optional<User> registeredUser = userService.add(email, encodedPassword, name, isModerator);
        if (registeredUser.isPresent()) {
            execution.setVariable("userRegistered", true);
            LOGGER.info("User registered successfully: {}", email);
        } else {
            execution.setVariable("userRegistrationFailed", true);
            LOGGER.warn("User registration failed for email: {}", email);
        }
    }
}