package lt.sus.Studiosus.model.enums;

import lombok.Getter;

@Getter
public enum Role {
  EMAIL(0),
  VIEWER(1),
  EDITOR(2),
  OWNER(3);

  private final int value;

  Role(int value) {
    this.value = value;
  }

  public static Role getRoleByValue(int value) {
    for (Role role : Role.values()) {
      if (role.getValue() == value) {
        return role;
      }
    }
    throw new IllegalArgumentException("Invalid role value");
  }

  public static Role getRoleByName(String name) {
    for (Role role : Role.values()) {
      if (role.name().equals(name)) {
        return role;
      }
    }
    throw new IllegalArgumentException("Invalid role name");
  }
}
