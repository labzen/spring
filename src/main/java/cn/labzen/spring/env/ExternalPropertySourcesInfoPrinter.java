package cn.labzen.spring.env;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

@Slf4j
public class ExternalPropertySourcesInfoPrinter implements ApplicationListener<ApplicationEvent> {

  @Override
  public void onApplicationEvent(@NotNull ApplicationEvent e) {
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
