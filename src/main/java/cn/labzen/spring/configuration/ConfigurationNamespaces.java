package cn.labzen.spring.configuration;

public class ConfigurationNamespaces {

  private static final String PREFIX_LABZEN = "spring.labzen";
  /**
   * 加载外部配置文件的方式，需要改一下，使用spring官方的外部配置指定方式，只是对配置文件的解密做增强
   */
  @Deprecated
  public static final String PREFIX_EXTERNAL_CONFIGURATION_PROPERTIES = PREFIX_LABZEN + ".env.external";
}
