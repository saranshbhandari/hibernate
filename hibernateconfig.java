import java.util.Properties;
import javax.sql.DataSource;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableTransactionManagement
@PropertySource("classpath:database.properties") // Loads the external properties file
public class HibernateConfig {

    @Autowired
    private Environment env; // For accessing properties

    /**
     * Creates and configures the HikariCP DataSource, utilizing connection pooling.
     */
    @Bean
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        
        // Basic JDBC settings
        dataSource.setDriverClassName(env.getProperty("jdbc.driver"));
        dataSource.setJdbcUrl(env.getProperty("jdbc.url"));
        dataSource.setUsername(env.getProperty("jdbc.user"));
        dataSource.setPassword(env.getProperty("jdbc.pass"));

        // HikariCP specific settings (using properties defined in the file)
        dataSource.setMaximumPoolSize(
            Integer.parseInt(env.getProperty("hikari.maximumPoolSize", "10"))
        );
        dataSource.setMinimumIdle(
            Integer.parseInt(env.getProperty("hikari.minimumIdle", "5"))
        );
        dataSource.setIdleTimeout(
            Long.parseLong(env.getProperty("hikari.idleTimeout", "30000"))
        );
        dataSource.setPoolName(env.getProperty("hikari.poolName", "HikariCP"));

        return dataSource;
    }

    /**
     * Configures the LocalSessionFactoryBean for Hibernate,
     * specifying the packages to scan and additional properties.
     */
    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        
        // Use the configured HikariCP data source
        sessionFactory.setDataSource(dataSource());
        
        // Specify the packages where your entity classes reside
        sessionFactory.setPackagesToScan("com.example.yourpackage.model");
        
        // Set Hibernate-specific properties (dialect, show_sql, etc.)
        sessionFactory.setHibernateProperties(hibernateProperties());
        
        return sessionFactory;
    }

    /**
     * Sets up the Hibernate transaction manager.
     */
    @Bean
    public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) {
        HibernateTransactionManager txManager = new HibernateTransactionManager();
        txManager.setSessionFactory(sessionFactory);
        return txManager;
    }

    /**
     * Constructs Hibernate properties from the external properties file.
     */
    private Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.dialect", env.getProperty("hibernate.dialect"));
        properties.put("hibernate.show_sql", env.getProperty("hibernate.show_sql"));
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        // You can add more Hibernate properties here if needed
        return properties;
    }
}
