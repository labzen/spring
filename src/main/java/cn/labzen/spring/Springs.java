package cn.labzen.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class Springs {

  private static volatile ConfigurableApplicationContext applicationContext;
  private static volatile ConfigurableListableBeanFactory listableBeanFactory;
  private static volatile ConfigurableEnvironment environment;

  private Springs() {
  }

  /**
   * 检查 Spring 上下文是否已初始化
   */
  private static void assertInitialized() {
    if (applicationContext == null) {
      throw new IllegalStateException("Spring ApplicationContext has not been initialized. " +
                                      "Ensure LabzenSpringHelperInitializer is registered in spring.factories.");
    }
  }

  static void setApplicationContext(@NonNull ConfigurableApplicationContext applicationContext) {
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
    assertInitialized();
    return applicationContext.getClassLoader();
  }

  /**
   * 通过类获取 Spring Bean
   */
  public static <T> Optional<T> bean(Class<T> type) {
    // 添加参数检查
    if (type == null) {
      return Optional.empty();
    }

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
    // 添加参数检查
    if (name == null || name.isEmpty()) {
      return Optional.empty();
    }

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
    // 添加参数检查
    if (name == null || name.isEmpty() || type == null) {
      return Optional.empty();
    }

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
    // 添加参数检查
    if (type == null) {
      return Collections.emptyMap();
    }

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
    // 添加参数检查
    if (type == null) {
      return Collections.emptyList();
    }

    return Arrays.asList(applicationContext.getBeanNamesForType(type));
  }

  /**
   * 动态实例一个类并注册该 Bean 到 Spring 容器
   */
  public static <T> T register(@NonNull Class<T> type) throws BeansException {
    String simpleName = type.getSimpleName();
    String name = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
    return register(name, type);
  }

  /**
   * 动态实例一个类并使用指定的name注册该 Bean 到 Spring 容器
   * Bean名称基于类的全限定名生成，避免同名冲突
   */
  public static <T> T register(@NonNull String name, @NonNull Class<T> type) throws BeansException {
    // 添加参数检查
    if (name.isEmpty()) {
      throw new IllegalArgumentException("Bean name cannot be empty");
    }

    T bean = listableBeanFactory.createBean(type);
    listableBeanFactory.registerSingleton(name, bean);
    return bean;
  }

  /**
   * 动态注册一个 Bean 到 Spring 容器
   * Bean名称基于类的全限定名生成，避免同名冲突
   */
  public static <T> T register(@NonNull T bean) {
    String fullName = bean.getClass().getName();
    String simpleName = bean.getClass().getSimpleName();
    // 使用全限定名的hashCode作为唯一标识，避免简单类名冲突
    String name = simpleName + "-" + Math.abs(fullName.hashCode());
    return register(name, bean);
  }

  /**
   * 动态使用指定的name注册一个 Bean 到 Spring 容器
   */
  public static <T> T register(@NonNull String name, @NonNull T bean) {
    // 添加参数检查
    if (name.isEmpty()) {
      throw new IllegalArgumentException("Bean name cannot be empty");
    }

    listableBeanFactory.autowireBeanProperties(bean, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
    listableBeanFactory.registerSingleton(name, bean);
    return bean;
  }

  /**
   * 注销一个类在 Spring 容器中注册的 Bean
   */
  public static void unregister(Class<?> type) {
    // 添加参数检查
    if (type == null) {
      return;
    }

    bean(type).ifPresent(o -> listableBeanFactory.destroyBean(o));
  }

  /**
   * 注销在 Spring 容器中注册的 Bean
   * 根据Bean类型选择合适的销毁方式
   */
  public static void unregister(String name) {
    // 添加参数检查
    if (name == null || name.isEmpty()) {
      return;
    }

    // 检查Bean是否存在
    if (!listableBeanFactory.containsSingleton(name)) {
      return;
    }

    // 通过 AutowireCapableBeanFactory 获取 SingletonBeanRegistry 功能
    // AutowireCapableBeanFactory 扩展了 SingletonBeanRegistry
    if (listableBeanFactory instanceof DefaultListableBeanFactory factory) {
      factory.destroySingleton(name);
      return;
    }

    // 降级方案：使用 destroyBean
    try {
      Object bean = listableBeanFactory.getBean(name);
      listableBeanFactory.destroyBean(name, bean);
    } catch (BeansException ignored) {
      // 忽略销毁错误
    }
  }

  /**
   * 获取 Spring 容器中的 Bean，如果不存在则动态注册并返回该 Bean
   * 注意：此方法非线程安全，建议在单线程或加锁环境下使用
   */
  public static <T> T getOrCreate(@NonNull Class<T> type) {
    assertInitialized();

    Optional<T> bean = bean(type);
    return bean.orElseGet(() -> register(type));
  }

  /**
   * 获取 Spring 容器中的 Bean，如果不存在则动态注册并返回该 Bean
   * 注意：此方法非线程安全，建议在单线程或加锁环境下使用
   */
  public static <T> T getOrCreate(@NonNull String name, @NonNull Class<T> type) {
    assertInitialized();

    // 添加参数检查
    if (name.isEmpty()) {
      throw new IllegalArgumentException("Bean name cannot be empty");
    }

    Optional<T> bean = bean(name, type);
    return bean.orElseGet(() -> register(name, type));
  }

  /**
   * 获取 Spring Application 名称
   */
  public static String applicationName() {
    assertInitialized();
    return environmentProperty("spring.application.name");
  }

  /**
   * 获取当前 Spring 激活的环境配置
   */
  public static List<String> activatedProfiles() {
    assertInitialized();
    return Arrays.asList(environment.getActiveProfiles());
  }

  /**
   * 判断当前 Spring 的环境配置是否激活
   */
  public static boolean isProfileActivated(String name) {
    // 添加参数检查
    if (name == null || name.isEmpty()) {
      return false;
    }

    return activatedProfiles().contains(name);
  }

  /**
   * 获取 Spring 环境属性
   */
  public static String environmentProperty(String name, @Nullable String defaultValue) {
    // 添加参数检查
    if (name == null || name.isEmpty()) {
      return defaultValue;
    }

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
   * 根据注解扫描符合条件的类，可在 {@link Consumer} 中自定义扫描条件
   * 扫描过程中遇到无法加载的类时会跳过并记录警告
   */
  @SafeVarargs
  public static Set<Class<?>> scanClassesByAnnotation(String pkg,
                                                      Class<?> type,
                                                      Class<Annotation>... annotationClasses) {
    // 添加参数检查
    if (pkg == null || pkg.isEmpty() || type == null || annotationClasses == null) {
      return Collections.emptySet();
    }

    return scanClasses(pkg, provider -> {
      provider.addIncludeFilter(new AssignableTypeFilter(type));
      for (Class<Annotation> annotationClass : annotationClasses) {
        provider.addIncludeFilter(new AnnotationTypeFilter(annotationClass));
      }
    });
  }

  /**
   * 扫描指定包下的类，可在 {@link Consumer} 中自定义扫描条件
   * 扫描过程中遇到无法加载的类时会跳过并记录警告
   */
  public static Set<Class<?>> scanClasses(String pkg, Consumer<ClassPathScanningCandidateComponentProvider> consumer) {
    // 添加参数检查
    if (pkg == null || pkg.isEmpty() || consumer == null) {
      return Collections.emptySet();
    }

    assertInitialized();

    ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
    provider.setEnvironment(environment);
    provider.setResourceLoader(applicationContext);

    consumer.accept(provider);
    return provider.findCandidateComponents(pkg).stream().map(beanDefinition -> {
      String className = beanDefinition.getBeanClassName();
      if (className == null) {
        return null;
      }
      try {
        return ClassUtils.forName(className, applicationContext.getClassLoader());
      } catch (ClassNotFoundException e) {
        // 记录警告并跳过无法加载的类，不中断整个扫描过程
        org.slf4j.LoggerFactory.getLogger(Springs.class)
                               .warn("无法加载扫描到的类: {}, 原因: {}", className, e.getMessage());
        return null;
      }
    }).filter(Objects::nonNull).collect(Collectors.toSet());
  }

}
