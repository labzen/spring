package cn.labzen.spring.exception;

import cn.labzen.meta.exception.LabzenRuntimeException;

/**
 * Spring 配置相关的运行时异常
 * 用于封装 Spring 配置过程中的各类异常信息
 */
public class SpringConfigurationException extends LabzenRuntimeException {

  public SpringConfigurationException(String message) {
    super(message);
  }

  public SpringConfigurationException(String message, Object... arguments) {
    super(message, arguments);
  }

  public SpringConfigurationException(Throwable cause) {
    super(cause);
  }

  public SpringConfigurationException(Throwable cause, String message) {
    super(cause, message);
  }

  public SpringConfigurationException(Throwable cause, String message, Object... arguments) {
    super(cause, message, arguments);
  }
}
