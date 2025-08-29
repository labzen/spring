package cn.labzen.spring.exception;

import cn.labzen.meta.exception.LabzenRuntimeException;

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
