package cn.labzen.spring.helper;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Springs {

  private static ConfigurableApplicationContext applicationContext;
  private static ConfigurableListableBeanFactory listableBeanFactory;
  private static ConfigurableEnvironment environment;

  private Springs() {
  }

  static void setApplicationContext(ConfigurableApplicationContext applicationContext) {
    Springs.applicationContext = applicationContext;
    Springs.listableBeanFactory = applicationContext.getBeanFactory();
    Springs.environment = applicationContext.getEnvironment();
  }

  public ClassLoader getSpringClassLoader() {
    return applicationContext.getClassLoader();
  }

  public <T> Optional<T> bean(Class<T> type) {
    try {
      T bean = applicationContext.getBean(type);
      return Optional.of(bean);
    } catch (BeansException e) {
      return Optional.empty();
    }
  }

  public <T> Map<String, T> beans(Class<T> type) {
    try {
      return applicationContext.getBeansOfType(type);
    } catch (BeansException e) {
      return Collections.emptyMap();
    }
  }

  public <T> List<String> beanNames(Class<T> type) {
    return Arrays.asList(applicationContext.getBeanNamesForType(type));
  }

  public <T> T register(Class<T> type) {
    Object bean = listableBeanFactory.createBean(type, AbstractBeanDefinition.AUTOWIRE_BY_TYPE, true);
    listableBeanFactory.registerSingleton(type.getSimpleName(), bean);
    //noinspection unchecked
    return (T) bean;
  }

  public <T> T getOrCreate(Class<T> type) {
    Optional<T> bean = bean(type);
    return bean.orElseGet(() -> register(type));
  }

  public List<String> activatedProfiles() {
    return Arrays.asList(environment.getActiveProfiles());
  }

  public boolean isProfileActivated(String name) {
    return activatedProfiles().contains(name);
  }

  public String environmentProperty(String name, String defaultValue) {
    return environment.getProperty(name, defaultValue);
  }

  public String environmentProperty(String name) {
    return environmentProperty(name, null);
  }

  public Set<Class<?>> scanClasses(String pkg, Class<?> type, Class<Annotation>... annotationClasses) {
    return scanClasses(pkg, provider -> {
      provider.addIncludeFilter(new AssignableTypeFilter(type));
      for (Class<Annotation> annotationClass : annotationClasses) {
        provider.addIncludeFilter(new AnnotationTypeFilter(annotationClass));
      }
    });
  }

  public Set<Class<?>> scanClasses(String pkg, Consumer<ClassPathScanningCandidateComponentProvider> consumer) {
    ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
    provider.setEnvironment(environment);
    provider.setResourceLoader(applicationContext);

    consumer.accept(provider);
    return provider.findCandidateComponents(pkg).stream().map(beanDefinition -> {
      try {
        return ClassUtils.forName(Objects.requireNonNull(beanDefinition.getBeanClassName()),
            applicationContext.getClassLoader());
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }).collect(Collectors.toSet());
  }
}
