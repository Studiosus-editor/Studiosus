package lt.sus.Studiosus.config;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class DataSourceConfig {

  @Bean
  @Profile("production")
  public DataSource postgresDataSource() {
    return DataSourceBuilder.create()
        .driverClassName("org.postgresql.Driver")
        .url("jdbc:postgresql://db:5432/studiosus")
        .username("postgres")
        .password("postgres")
        .build();
  }

  @Bean
  @Profile("!production")
  public DataSource h2DataSource() {
    return DataSourceBuilder.create()
        .driverClassName("org.h2.Driver")
        .url("jdbc:h2:file:~/studiosus;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
        .username("sa")
        .password("")
        .build();
  }
}
