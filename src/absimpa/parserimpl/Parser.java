package absimpa.parserimpl;

import absimpa.*;


/**
 * <p>
 * A recursive decent parser which transforms information provided by a
 * {@link absimpa.Lexer} into some type {@code N}.
 * </p>
 * <p>
 * To obtain a {@code Parser}, you need to create a {@link Grammar}, for
 * example by using the {@link GrammarBuilder} and then call
 * {@link Grammar#compile()}.
 * </p>
 * 
 * @param <N> is the type of the objects created by the parser
 * @param <C> is the type of token codes provided by the lexer
 * @param <L> is the type of {@link absimpa.Lexer} from which the parser
 *        obtains tokens information
 */
public abstract class Parser<N,C extends Enum<C>,L extends Lexer<C>>
    implements ParserI<N,C,L>
{
  private Parser() { }

   public abstract N parse(L lex) throws ParseException;
}
