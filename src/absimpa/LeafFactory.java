package absimpa;

/**
 * <p>
 * necessary to create leaf nodes from parsed tokens. Whenever a
 * {@link Parser} recognizes a token by looking at the token code {@code C}
 * provided by the {@link Lexer}, it will call its {@code LeafFactory} with
 * the lexer to transform the current token into an {@code N}.
 * </p>
 * 
 * @param <N> is the type of objects created by the factory
 * @param <L> is the lexer from which we can obtain token information to
 *        create a leaf node
 * @param <C> is the type of token codes generated by the lexer
 */
public interface LeafFactory<N, C extends Enum<C>, L extends Lexer<C>> {
  /**
   * <p>
   * must create an {@code N} from the current token available from the
   * lexer. This method should normally <b>not</b> call {@link Lexer#next}.
   * </p>
   */
  N create(L lex);
}
