package absimpa;

/**
 * <p>
 * parses input in the form of token codes as provided by a lexical analyzer
 * and constructs result obejcts. To obtain a parser, use the
 * {@link GrammarBuilder} and {@link Grammar#compile compile} the resulting {@link Grammar}.
 * </p>
 * 
 * @param <N> is the type of the objects created by the parser
 * @param <C> is the type of token codes provided by the lexer
 * @param <L> is the type of {@link absimpa.Lexer} from which the parser
 *        obtains token codes
 */
public interface Parser<N,C extends Enum<C>,L extends Lexer<C>> {
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
   *         by the {@code Lexer} does not match the grammar for which this
   *         parser was created.
   */
  N parse(L lex) throws ParseException;

}
