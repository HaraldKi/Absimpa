package absimpa;

/**
* @param <N> is the type of the objects created by the parser
* @param <C> is the type of token codes provided by the lexer
* @param <L> is the type of {@link absimpa.Lexer} from which the parser
*        obtains tokens information
*/
public interface ParserI<N,C extends Enum<C>,L extends Lexer<C>> {
  N parse(L lex) throws ParseException;
}
