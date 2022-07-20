package cn.labzen.spring.exception;

import cn.labzen.cells.core.exception.LabzenRuntimeException;
import org.jetbrains.annotations.NotNull;

public class SpringConfigurationException extends LabzenRuntimeException {

  public SpringConfigurationException(@NotNull String message) {
    super(message);
  }

  public SpringConfigurationException(@NotNull String message, @NotNull Object... arguments) {
    super(message, arguments);
  }

  public SpringConfigurationException(@NotNull Throwable cause) {
    super(cause);
  }

  public SpringConfigurationException(@NotNull Throwable cause, @NotNull String message) {
    super(cause, message);
  }

  public SpringConfigurationException(@NotNull Throwable cause, @NotNull String message, @NotNull Object... arguments) {
    super(cause, message, arguments);
  }
}
