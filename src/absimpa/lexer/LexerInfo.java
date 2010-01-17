package absimpa.lexer;

/**
 * <p>
 * provides additional information about an enum for
 * {@link SimpleLexer#SimpleLexer(Class, example.LeafFactory)} to
 * automatically set up the lexical analysis for all tokens of the enum.</p.
 * 
 * @param <C> is the enum which gets for information attached.
 */
public interface LexerInfo<C extends Enum<C>> {
  String getRegex();
  C eofCode();
}
