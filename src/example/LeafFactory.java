package example;

import absimpa.ParseException;
import absimpa.bnf.SimpleLexer;

public interface LeafFactory<N, C extends Enum<C>> {
  N create(SimpleLexer<N,C> lex) throws ParseException;
}
