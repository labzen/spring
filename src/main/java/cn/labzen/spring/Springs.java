package cn.labzen.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
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

  /**
   * 获取 {@link ApplicationContext}
   */
  public static ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  /**
   * 获取 {@link ListableBeanFactory}
   */
  public static ListableBeanFactory getListableBeanFactory() {
    return listableBeanFactory;
  }

  /**
   * 获取 Spring {@link ApplicationContext} 的 {@link ClassLoader}
   */
  public static ClassLoader getSpringClassLoader() {
    return applicationContext.getClassLoader();
  }

  /**
   * 通过类获取 Spring Bean
   */
  public static <T> Optional<T> bean(Class<T> type) {
    try {
      T bean = listableBeanFactory.getBean(type);
      return Optional.of(bean);
    } catch (BeansException e) {
      return Optional.empty();
    }
  }

  /**
   * 通过 Bean name 获取 Spring Bean
   */
  public static Optional<?> bean(String name) {
    try {
      Object bean = listableBeanFactory.getBean(name);
      return Optional.of(bean);
    } catch (BeansException e) {
      return Optional.empty();
    }
  }

  /**
   * 通过类以及 Bean name 精确获取 Spring Bean
   */
  public static <T> Optional<T> bean(String name, Class<T> type) {
    try {
      T bean = listableBeanFactory.getBean(name, type);
      return Optional.of(bean);
    } catch (BeansException e) {
      return Optional.empty();
    }
  }

  /**
   * 通过类获取注册的所有 Spring Bean
   */
  public static <T> Map<String, T> beans(Class<T> type) {
    try {
      return listableBeanFactory.getBeansOfType(type);
    } catch (BeansException e) {
      return Collections.emptyMap();
    }
  }

  /**
   * 获取类在 Spring 容器中注册的所有 Bean name
   */
  public static <T> List<String> beanNames(Class<T> type) {
    return Arrays.asList(applicationContext.getBeanNamesForType(type));
  }

  /**
   * 动态实例一个类并注册该 Bean 到 Spring 容器
   */
  public static <T> T register(Class<T> type) throws BeansException {
    String simpleName = type.getSimpleName();
    String name = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
    return register(name, type);
  }

  /**
   * 动态实例一个类并使用指定的name注册该 Bean 到 Spring 容器
   */
  public static <T> T register(@NonNull String name, Class<T> type) throws BeansException {
    T bean = listableBeanFactory.createBean(type);
    listableBeanFactory.registerSingleton(name, bean);
    return bean;
  }

  /**
   * 动态注册一个 Bean 到 Spring 容器
   */
  public static <T> T register(T bean) {
    String simpleName = bean.getClass().getSimpleName();
    String name = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
    return register(name, bean);
  }

  /**
   * 动态使用指定的name注册一个 Bean 到 Spring 容器
   */
  public static <T> T register(@NonNull String name, T bean) {
    listableBeanFactory.autowireBeanProperties(bean, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
    listableBeanFactory.registerSingleton(name, bean);
    return bean;
  }

  /**
   * 注销一个类在 Spring 容器中注册的 Bean
   */
  public static void unregister(Class<?> type) {
    bean(type).ifPresent(o -> listableBeanFactory.destroyBean(o));
  }

  /**
   * 注销在 Spring 容器中注册的 Bean
   */
  public static void unregister(String name) {
    listableBeanFactory.destroyScopedBean(name);
  }

  /**
   * 获取 Spring 容器中的 Bean，如果不存在则动态注册并返回该 Bean
   */
  public static <T> T getOrCreate(Class<T> type) {
    Optional<T> bean = bean(type);
    return bean.orElseGet(() -> register(type));
  }

  /**
   * 获取 Spring 容器中的 Bean，如果不存在则动态注册并返回该 Bean
   */
  public static <T> T getOrCreate(String name, Class<T> type) {
    Optional<T> bean = bean(name, type);
    return bean.orElseGet(() -> register(name, type));
  }

  /**
   * 获取 Spring Application 名称
   */
  public static String applicationName() {
    return environmentProperty("spring.application.name");
  }

  /**
   * 获取当前 Spring 激活的环境配置
   */
  public static List<String> activatedProfiles() {
    return Arrays.asList(environment.getActiveProfiles());
  }

  /**
   * 判断当前 Spring 的环境配置是否激活
   */
  public static boolean isProfileActivated(String name) {
    return activatedProfiles().contains(name);
  }

  /**
   * 获取 Spring 环境属性
   */
  public static String environmentProperty(String name, @Nullable String defaultValue) {
    if (defaultValue == null) {
      return environment.getProperty(name);
    }
    return environment.getProperty(name, defaultValue);
  }

  /**
   * 获取 Spring 环境属性
   */
  public static String environmentProperty(String name) {
    return environmentProperty(name, null);
  }

  /**
   * 根据注解扫描符合条件的类
   */
  @SafeVarargs
  public static Set<Class<?>> scanClassesByAnnotation(String pkg,
                                                      Class<?> type,
                                                      Class<Annotation>... annotationClasses) {
    return scanClasses(pkg, provider -> {
      provider.addIncludeFilter(new AssignableTypeFilter(type));
      for (Class<Annotation> annotationClass : annotationClasses) {
        provider.addIncludeFilter(new AnnotationTypeFilter(annotationClass));
      }
    });
  }

  /**
   * 扫描指定包下的类，可在 {@link Consumer} 中自定义扫描条件
   */
  public static Set<Class<?>> scanClasses(String pkg, Consumer<ClassPathScanningCandidateComponentProvider> consumer) {
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
