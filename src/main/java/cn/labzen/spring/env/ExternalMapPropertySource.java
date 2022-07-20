package cn.labzen.spring.env;

import lombok.Getter;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

public class ExternalMapPropertySource extends MapPropertySource {

  @Getter
  private final String profile;
  @Getter
  private final boolean encrypted;

  public ExternalMapPropertySource(String profile, String name, Map<String, Object> source, boolean encrypted) {
    super(name, source);
    this.profile = profile;
    this.encrypted = encrypted;
  }
}
