package lt.sus.Studiosus.controllers.exception;

public class NoPermissionException extends RuntimeException {
  public NoPermissionException(String message) {
    super(message);
  }
}
