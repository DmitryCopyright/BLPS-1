package dmitryv.lab1.config;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;


@Configuration
@EnableTransactionManagement
public class TransactionsConfig {

    @Bean(initMethod = "init", destroyMethod = "close")
    public UserTransactionManager atomikosTransactionManager() {
        UserTransactionManager atomikosTransactionManager = new UserTransactionManager();
        atomikosTransactionManager.setForceShutdown(false);
        return atomikosTransactionManager;
    }

    @Bean
    public UserTransactionImp atomikosUserTransaction() {
        UserTransactionImp atomikosUserTransaction = new UserTransactionImp();
        return atomikosUserTransaction;
    }

    @Bean
    public JtaTransactionManager transactionManager(UserTransactionManager atomikosTransactionManager, UserTransactionImp atomikosUserTransaction) {
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
        jtaTransactionManager.setUserTransaction(atomikosUserTransaction);
        jtaTransactionManager.setTransactionManager(atomikosTransactionManager);
        return jtaTransactionManager;
    }
}