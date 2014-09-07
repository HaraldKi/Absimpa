package absimpa.lexer;

import absimpa.ParseException;
import absimpa.lexer.SimpleLexer;

public interface LeafFactory<N, C extends Enum<C>> {
  N create(SimpleLexer<N,C> lex) throws ParseException;
}
