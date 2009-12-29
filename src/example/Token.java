package example;

/**
 * <p>
 * provides a typical token as a pair of the token text and its type from an
 * enumeration <code>C</code>.
 * </p>
 */
public final class Token<N,C extends Enum<C> &LeafFactory<N,C>> {
  private final String text;
  private final C c;

  public Token(String text, C c) {
    this.text = text;
    this.c = c;
  }
  public C getCode() {
   return c;
  }

  public String getText() {
    return text;
  }
  public String toString() {
    return String.format("%s(\"%s\")", c, text);
  }
  
}