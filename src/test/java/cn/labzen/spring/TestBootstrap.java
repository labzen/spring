package cn.labzen.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class TestBootstrap implements EnvironmentAware {

  private Environment environment;

  public static void main(String[] args) {
    SpringApplication.run(TestBootstrap.class, args);
  }

  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }
}
