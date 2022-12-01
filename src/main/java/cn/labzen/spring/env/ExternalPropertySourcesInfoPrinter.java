package cn.labzen.spring.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.lang.NonNull;

public class ExternalPropertySourcesInfoPrinter implements ApplicationListener<ApplicationEvent> {

  private final Logger logger = LoggerFactory.getLogger(ExternalPropertySourcesInfoPrinter.class);

  @Override
  public void onApplicationEvent(@NonNull ApplicationEvent e) {
    if (e instanceof ApplicationPreparedEvent) {
      ApplicationPreparedEvent event = (ApplicationPreparedEvent) e;
      ConfigurableEnvironment environment = event.getApplicationContext().getEnvironment();

      MutablePropertySources propertySources = environment.getPropertySources();
      for (PropertySource<?> propertySource : propertySources) {
        if (propertySource instanceof ExternalMapPropertySource) {
          ExternalMapPropertySource emps = (ExternalMapPropertySource) propertySource;
          logger.info("已加载外部配置文件 Profile[{}]：{}", emps.getProfile(), emps.getName());
        }
      }
    }
  }
}
