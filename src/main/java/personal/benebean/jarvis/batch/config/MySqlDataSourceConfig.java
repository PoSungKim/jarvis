package personal.benebean.jarvis.batch.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
@ConfigurationPropertiesScan
@PropertySource(value = {"classpath:application.yml"})
@ConfigurationProperties(prefix = "spring.datasource.hikari")
@MapperScan(basePackages = "personal.benebean.jarvis.batch.mapper", sqlSessionFactoryRef = "mySqlSessionFactory")
public class MySqlDataSourceConfig {

    @Value("${driver-class-name}")
    String driverClassName;

    @Value("${jdbc-url}")
    String jdbcUrl;

    @Value("${username}")
    String userName;

    @Value("${password}")
    String password;

    @Value("${mybatis.mapper-locations}")
    String mapperLocations;

    @Bean
    public DataSource mySqlDataSource() {
        return DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .type(HikariDataSource.class) // not necessarily needed, but it would be good to specify it even if the Hikari CP is a default setting...
                .url(jdbcUrl)
                .username(userName)
                .password(password)
                .build();;
    }

    @Bean
    public SqlSessionFactory mySqlSessionFactory(@Qualifier("mySqlDataSource") DataSource dataSource) throws Exception {

        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);

        PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setMapperLocations(pathMatchingResourcePatternResolver.getResources(mapperLocations));

        //org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        //configuration.setDefaultExecutorType(ExecutorType.BATCH);
        //sqlSessionFactoryBean.setConfiguration(configuration);

        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    public DataSourceTransactionManager mySqlDataSourceTransactionManager(@Qualifier("mySqlDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public SqlSessionTemplate simpleSqlSession(@Qualifier("mySqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory, ExecutorType.SIMPLE);
    }

    @Bean
    public SqlSessionTemplate batchSqlSession(@Qualifier("mySqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws SQLException {

        return new SqlSessionTemplate(sqlSessionFactory, ExecutorType.BATCH);
    }

}