package absimpa;

/**
* @param <N> is the type of the objects created by the parser
* @param <C> is the type of token codes provided by the lexer
* @param <L> is the type of {@link absimpa.Lexer} from which the parser
*        obtains tokens information
*/
public interface ParserI<N,C extends Enum<C>,L extends Lexer<C>> {
  /**
   * <p>
   * parses a sequence of objects of type {@code <C>} from the given
   * {@code Lexer} and transforms it into an object of type {@code <N>}.
   * </p>
   * 
   * @return is a value created by a {@link NodeFactory} or
   *         {@link LeafFactory} and therefore may in particular be
   *         {@code null}.
   * 
   * @throws ParseException if the sequence of {@code <C>} objects provided
   *         by the {@code Lexer} does not match the grammarfor which this
   *         parser was created.
   */
  N parse(L lex) throws ParseException;
}
