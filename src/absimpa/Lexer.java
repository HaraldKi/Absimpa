package absimpa;

import java.util.Set;

/**
 * <p>
 * defines the interface to a lexical analyzer (lexer) needed by the
 * {@link Parser}. This interface does not define the kind of input the lexer
 * takes apart. It is only assumed that the lexer, as a result, can provide
 * objects of an enumeration type.
 * </p>
 * <p>
 * The {@code Lexer} must maintain a <em>current token code</em> which is
 * repeatedly queried by the parser. Only when {@link #next} is called, the
 * parser should discard the current token, and chop the next token off its
 * own input.
 * </p>
 * 
 * @param <C> is the type representing the <em>characters</em> of the language
 *        to be parsed
 */
public interface Lexer<N, C extends Enum<C>> {
  /**
   * <p>
   * returns a node of type N for the current token while advancing the
   * current token internally to the next one.
   * </p>
   * <p>
   * <b>NOTE:</b> It may seem strange at first to force the lexer to create
   * the leaf nodes. Rather we would let this do the parser. But then a lexer
   * implementation would need to return more than just the token code for
   * most tokens to be transformed into a node. This again would require a
   * general return type {@code Token<C>} which would then have to
   * implemented by each implementation. As a result the descendants of
   * {@code Token<C>} would have to be specified as a third type parameter at
   * many places.
   * </p>
   */
  N next() throws ParseException;
  /**
   * <p>
   * provides the current token code. This method must always return the
   * same token code as long as {@link #next} is not called.
   * </p>
   */
  C current();

  /**
   * <p>
   * creates a {@link ParseException} on request from the parser. This method
   * is called by the parser if it finds a token code that does not fit its
   * grammar. It is up to the {@code Lexer} implementation to provide as much
   * information as possible in the exception about the current position of
   * the input.</p>
   * 
   * @param expectedTokens a set of tokens that the parser would have
   *        expected at the current position.
   */
  ParseException parseException(Set<C> expectedTokens);
}
