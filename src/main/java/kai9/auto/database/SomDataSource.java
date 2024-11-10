package kai9.auto.database;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariDataSource;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.datasource.som")
@EnableJpaRepositories(basePackages = "kai9.som.repository", // リポジトリが存在するパッケージを指定
        entityManagerFactoryRef = "somEntityManagerFactory", transactionManagerRef = "somTransactionManager")
public class SomDataSource {

    private String driverClassName;
    private String url;
    private String username;
    private String password;
    private int maximumPoolSize;
    private int minimumIdle;

    @Bean(name = "somds")
    public DataSource createDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setMaximumPoolSize(maximumPoolSize);
        dataSource.setMinimumIdle(minimumIdle);
        return dataSource;
    }

    @Bean(name = "somjdbc")
    public JdbcTemplate createJdbcTemplate(@Qualifier("somds")
    DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "somEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean somEntityManagerFactory(
            @Qualifier("somds")
            DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("kai9.som.model"); // セカンダリのエンティティが存在するパッケージを指定
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        return em;
    }

    @Bean(name = "somTransactionManager")
    public PlatformTransactionManager somTransactionManager(
            @Qualifier("somEntityManagerFactory")
            EntityManagerFactory somEntityManagerFactory) {
        return new JpaTransactionManager(somEntityManagerFactory);
    }
}
