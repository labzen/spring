package cn.labzen.spring.env;

import org.springframework.core.env.MapPropertySource;

import java.util.Map;

public class ExternalMapPropertySource extends MapPropertySource {

  private final String profile;
  private final boolean encrypted;

  public ExternalMapPropertySource(String profile, String name, Map<String, Object> source, boolean encrypted) {
    super(name, source);
    this.profile = profile;
    this.encrypted = encrypted;
  }

  public String getProfile() {
    return profile;
  }

  public boolean isEncrypted() {
    return encrypted;
  }
}
