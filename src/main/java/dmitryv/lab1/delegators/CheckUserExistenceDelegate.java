package dmitryv.lab1.delegators;

import dmitryv.lab1.services.UserService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class CheckUserExistenceDelegate implements JavaDelegate {

    @Autowired
    private UserService userService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String email = (String) execution.getVariable("email");
        boolean userExists = userService.exist(email);
        execution.setVariable("userExists", userExists);
    }
}