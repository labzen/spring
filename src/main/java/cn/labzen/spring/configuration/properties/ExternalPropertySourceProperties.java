package cn.labzen.spring.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

import static cn.labzen.spring.configuration.ConfigurationNamespaces.PREFIX_EXTERNAL_CONFIGURATION_PROPERTIES;

/**
 * 配置如何加载外部配置属性，目标为YAML文件
 */
@SuppressWarnings("ConfigurationProperties")
@ConfigurationProperties(prefix = PREFIX_EXTERNAL_CONFIGURATION_PROPERTIES)
public class ExternalPropertySourceProperties {

  /**
   * 配置属性源地址：为网络地址时，将获取{uri}/application-{profile}文件；为本地文件地址时，指向的认为是目录，将获取该目录下的application-{profile}文件
   */
  private String uri;
  /**
   * 如果提供密码，代表外部配置属性源文件，已经经过加密，需要解密才能读取
   */
  private String password = null;
  /**
   * 激活的Profile，也可通过环境变量传入，例如：' mvn -jar xxx.jar --spring.profiles.active=devel '
   */
  private List<String> activeProfiles;

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public List<String> getActiveProfiles() {
    return activeProfiles;
  }

  public void setActiveProfiles(List<String> activeProfiles) {
    this.activeProfiles = activeProfiles;
  }
}
