package cn.labzen.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.Optional;

@SpringBootApplication
public class TestBootstrap implements EnvironmentAware {

  private Environment environment;

  public static void main(String[] args) {
    SpringApplication.run(TestBootstrap.class, args);
  }

  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;

    // 使用环境变量或系统属性获取敏感配置，避免硬编码
    String configUri = Optional.ofNullable(System.getenv("LABZEN_CONFIG_URI"))
        .orElseGet(() -> Optional.ofNullable(System.getProperty("labzen.config.uri"))
            .orElse("/Users/dean/Working/labzen/configs/spring/crypto"));
    String configPassword = Optional.ofNullable(System.getenv("LABZEN_CONFIG_PASSWORD"))
        .orElseGet(() -> System.getProperty("labzen.config.password", ""));

    System.out.println("加载的外部配置项 config.uri: " + configUri);
    if (!configPassword.isEmpty()) {
      System.out.println("加载的外部配置项 config.password: ****");
    }
  }
}
