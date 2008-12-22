package absimpa;

import java.util.Set;

/**
 * <p>
 * defines the interface to a lexical analyzer (lexer) needed by the
 * {@link Parser}. This interface does not define the kind of input the
 * lexer takes apart. It is only assumed that the lexer, as a result, can
 * provide objects of an enumeration type.
 * </p>
 * <p>
 * The {@code Lexer} must maintain a <em>current token code</em> which is
 * repeatedly queried by the parser. Only when {@link #next} is called, the
 * parser should discard the current token, and chop the next token off its
 * own input.
 * </p>
 * 
 * @param <TokenCode> is the type representing the
 *        <q>characters</q>
 *        of the language to be parsed
 */
public interface Lexer<TokenCode extends Enum<TokenCode>> {
  /**
   * <p>
   * must discard the current token code, create a new one and return it.
   * </p>
   */
  TokenCode next();
  /**
   * <p>
   * must provide the current token code. This method must always return the
   * same token code as long as {@link #next} is not called.
   * </p>
   */
  TokenCode current();

  /**
   * <p>
   * must provide a {@link ParseException}. This method is called by the
   * parser if it finds a token code that does not fit its grammar. It is up
   * to the {@code Lexer} implementation to provide as much information as
   * possible in the exception about the current position of the input.</p.
   * 
   * @param expectedTokens a set of tokens that the parser would have
   *        expected at the current position.
   */
  ParseException parseException(Set<TokenCode> expectedTokens);
}
