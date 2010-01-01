package example;

import absimpa.ParseException;

public interface LeafFactory<N, C extends Enum<C>> {
  N create(TrivialLexer<N,C> lex) throws ParseException;
}
