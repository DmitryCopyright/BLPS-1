package dmitryv.lab1.config;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.sql.DataSource;


@Configuration
public class CamundaConfig {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JtaTransactionManager transactionManager;

    @Bean
    public SpringProcessEngineConfiguration processEngineConfiguration() {
        SpringProcessEngineConfiguration config = new SpringProcessEngineConfiguration();

        config.setDataSource(dataSource);
        config.setTransactionManager(transactionManager);
        config.setTransactionsExternallyManaged(true);
        config.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        config.setHistory(ProcessEngineConfiguration.HISTORY_FULL);
        config.setJobExecutorActivate(true);
        config.setMetricsEnabled(false);

        // Добавление ресурсов
        config.setDeploymentResources(new Resource[]{
                new ClassPathResource("diagram_1.bpmn"),
                new ClassPathResource("static/forms/registerUser.form"),
                new ClassPathResource("static/forms/subscribeToTopic.form")
        });

        return config;
    }
}