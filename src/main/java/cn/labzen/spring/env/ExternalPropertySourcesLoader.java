package cn.labzen.spring.env;

import cn.labzen.spring.configuration.properties.ExternalPropertySourceProperties;
import cn.labzen.spring.exception.SpringConfigurationException;
import org.springframework.beans.factory.config.YamlProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;
import oshi.util.tuples.Pair;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

import static cn.labzen.spring.configuration.ConfigurationNamespaces.PREFIX_EXTERNAL_CONFIGURATION_PROPERTIES;

public class ExternalPropertySourcesLoader implements EnvironmentPostProcessor {

  private boolean isURL;
  private String cryptoPassword;
  private boolean isCryptoSource;
  private String extension;

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
    ExternalPropertySourceProperties ecp = extract(environment);
    if (ecp == null) {
      return;
    }

    List<Pair<String, PropertySource<?>>> propertySources = loadExternalPropertySources(ecp);

    propertySources.forEach(pair -> {
      environment.addActiveProfile(pair.getA());
      environment.getPropertySources().addLast(pair.getB());
    });
  }

  private ExternalPropertySourceProperties extract(ConfigurableEnvironment environment) {
    String uri = environment.getProperty(PREFIX_EXTERNAL_CONFIGURATION_PROPERTIES + ".uri");
    if (uri == null) {
      return null;
    }
    cryptoPassword = environment.getProperty(PREFIX_EXTERNAL_CONFIGURATION_PROPERTIES + ".password");

    String[] activeProfiles = environment.getActiveProfiles();
    Set<String> profiles = new LinkedHashSet<>(Arrays.asList(activeProfiles));
    int profileIndex = 0;
    while (true) {
      String key = PREFIX_EXTERNAL_CONFIGURATION_PROPERTIES + ".active-profiles[" + profileIndex + "]";
      String profile = environment.getProperty(key);
      if (profile == null) {
        break;
      }
      profiles.add(profile);
      profileIndex++;
    }

    ExternalPropertySourceProperties ecp = new ExternalPropertySourceProperties();
    ecp.setUri(uri);
    ecp.setPassword(cryptoPassword);
    ecp.setActiveProfiles(new ArrayList<>(profiles));
    return ecp;
  }

  @SuppressWarnings("HttpUrlsUsage")
  private List<Pair<String, PropertySource<?>>> loadExternalPropertySources(ExternalPropertySourceProperties ecp) {
    String uri = ecp.getUri().toLowerCase();
    isURL = uri.startsWith("http://") || uri.startsWith("https://");

    isCryptoSource = cryptoPassword != null && !cryptoPassword.isBlank();
    if (isCryptoSource) {
      extension = ".cfg";
    } else {
      extension = ".yml";
    }

    return ecp.getActiveProfiles()
              .stream()
              .map(profileName -> new Pair<String, PropertySource<?>>(profileName,
                  loadExternalPropertySources(ecp.getUri(), profileName)))
              .collect(Collectors.toList());
  }

  private PropertySource<?> loadExternalPropertySources(String uri, String profile) {
    String fileName;
    if ("default".equalsIgnoreCase(profile)) {
      fileName = "application" + extension;
    } else {
      fileName = "application-" + profile + extension;
    }

    byte[] propertiesContent;
    if (isURL) {
      propertiesContent = loadSourceContentFromURL(uri + "/" + fileName);
    } else {
      propertiesContent = loadSourceContentFromLocal(uri + File.separator + fileName);
    }

    String contentString;
    if (isCryptoSource) {
      Crypto crypto = new Crypto(cryptoPassword);
      try {
        byte[] decryptedContent = crypto.decrypt(propertiesContent);
        contentString = new String(decryptedContent);
      } catch (RuntimeException e) {
        throw new SpringConfigurationException(e, "外部加密配置文件解密异常");
      }
      //cipher = Ciphers.symmetrical().withKey(ecp.getPassword()).withIVParameter(iv);
    } else {
      contentString = new String(propertiesContent);
    }

    Map<String, Object> properties = loadYamlAsMap(contentString);

    String name = "Config resource 'external resource [" + fileName + "]' via location '" + uri + "'";
    return new ExternalMapPropertySource(profile, name, properties, isCryptoSource);
  }

  private Map<String, Object> loadYamlAsMap(String content) {
    Yaml yaml = new Yaml();
    Object yamlObject = yaml.load(content);

    Map<String, Object> objectMap = asMap(yamlObject);
    Map<String, Object> result = new LinkedHashMap<>();
    buildFlattenedMap(result, objectMap, null);

    return result;
  }

  /**
   * from spring class: {@link YamlProcessor}
   */
  @SuppressWarnings("unchecked")
  private Map<String, Object> asMap(Object object) {
    // YAML can have numbers as keys
    Map<String, Object> result = new LinkedHashMap<>();
    if (!(object instanceof Map)) {
      // A document can be a text literal
      result.put("document", object);
      return result;
    }

    Map<Object, Object> map = (Map<Object, Object>) object;
    map.forEach((key, value) -> {
      if (value instanceof Map) {
        value = asMap(value);
      }
      if (key instanceof CharSequence) {
        result.put(key.toString(), value);
      } else {
        // It has to be a map key in this case
        result.put("[" + key.toString() + "]", value);
      }
    });
    return result;
  }

  @SuppressWarnings("unchecked")
  private void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source, String path) {
    source.forEach((key, value) -> {
      if (StringUtils.hasText(path)) {
        if (key.startsWith("[")) {
          key = path + key;
        } else {
          key = path + '.' + key;
        }
      }
      switch (value) {
        case String str -> result.put(key, str);
        case Map<?, ?> map -> buildFlattenedMap(result, (Map<String, Object>) map, key); // Need a compound key
        case Collection<?> collection -> {
          // Need a compound key
          if (collection.isEmpty()) {
            result.put(key, "");
          } else {
            int count = 0;
            for (Object object : collection) {
              buildFlattenedMap(result, Collections.singletonMap("[" + (count++) + "]", object), key);
            }
          }
        }
        default -> result.put(key, value);
      }
    });
  }

  private byte[] loadSourceContentFromURL(String value) {
    try {
      URL url = URI.create(value).toURL();
      if ("https".equalsIgnoreCase(url.getProtocol())) {
        ignoreSSL();
      }

      try (InputStream inputStream = url.openStream()) {
        return inputStream.readAllBytes();
      }
    } catch (Exception e) {
      throw new SpringConfigurationException(e, "无法读取外部Spring配置文件：{}", value);
    }
  }

  private byte[] loadSourceContentFromLocal(String value) {
    File file = new File(value);
    if (!file.exists() || !file.isFile() || !file.canRead()) {
      throw new SpringConfigurationException("找不到或无法读取外部Spring配置文件：{}", value);
    }

    try (FileInputStream fis = new FileInputStream(file)) {
      return fis.readAllBytes();
    } catch (IOException e) {
      throw new SpringConfigurationException(e, "读取外部Spring配置文件失败：{}", value);
    }
  }

  /**
   * todo 临时方案
   */
  private void ignoreSSL() throws NoSuchAlgorithmException, KeyManagementException {
    HostnameVerifier hv = (hostname, session) -> true;
    TrustManager[] trustAllCerts = new TrustManager[]{new IgnoringTrustManager()};
    SSLContext context = SSLContext.getInstance("SSL");
    context.init(null, trustAllCerts, null);
    HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
    HttpsURLConnection.setDefaultHostnameVerifier(hv);
  }

  public static class IgnoringTrustManager implements TrustManager, X509TrustManager {

    public X509Certificate[] getAcceptedIssuers() {
      return null;
    }

    public void checkServerTrusted(X509Certificate[] certs, String authType) {
      // Do nothing. Just allow them all.
    }

    public void checkClientTrusted(X509Certificate[] certs, String authType) {
      // Do nothing. Just allow them all.
    }

  }

}
