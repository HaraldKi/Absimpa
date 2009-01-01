package absimpa;
/**
 * thrown by {@link Grammar#compile}.
 */
public class LeftRecursiveException extends RuntimeException {
  public LeftRecursiveException(String msg) {
    super(msg);
  }
}
