package lt.sus.Studiosus.model.enums;

import lombok.Getter;

@Getter
public enum LogLevel {
  INFO(0),
  WARN(1),
  FATAL(2);

  private final int value;

  LogLevel(int value) {
    this.value = value;
  }
}
