package cn.labzen.spring.helper;

import cn.labzen.meta.spring.SpringApplicationContextInitializerOrder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;

public class LabzenSpringHelperInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>,
                                                      Ordered {

  @Override
  public int getOrder() {
    return SpringApplicationContextInitializerOrder.MODULE_SPRING_INITIALIZER_ORDER;
  }

  @Override
  public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
    Springs.setApplicationContext(applicationContext);
  }
}
