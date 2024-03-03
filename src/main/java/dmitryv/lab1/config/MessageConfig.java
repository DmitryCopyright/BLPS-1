package dmitryv.lab1.config;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Properties;

@Configuration
@EnableJpaRepositories(basePackages = "dmitryv.lab1.repos",
        entityManagerFactoryRef = "messageEntityManager",
        transactionManagerRef = "transactionManager")
@EnableTransactionManagement
public class MessageConfig {

    @Bean(initMethod = "init", destroyMethod = "close")
    public AtomikosDataSourceBean messageDatasource() {
        AtomikosDataSourceBean dataSourceBean = new AtomikosDataSourceBean();
        dataSourceBean.setUniqueResourceName("messageDataSource");
        dataSourceBean.setXaDataSourceClassName("org.postgresql.xa.PGXADataSource");

        Properties xaProperties = new Properties();
        xaProperties.put("user", "s335065");
        xaProperties.put("password", "RnIXdSSUHSXRZDkr");
        xaProperties.put("url", "jdbc:postgresql://localhost:5432/studs");
        dataSourceBean.setXaProperties(xaProperties);

        return dataSourceBean;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean messageEntityManager() {
        LocalContainerEntityManagerFactoryBean entityManager = new LocalContainerEntityManagerFactoryBean();
        entityManager.setDataSource(messageDatasource());
        entityManager.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        entityManager.setPackagesToScan("dmitryv.lab1.models");
        return entityManager;
    }
}
