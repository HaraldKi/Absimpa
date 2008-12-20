package grammar;


public interface LeafFactory<N, C extends Enum<C>, L extends Lexer<C>> {
  N create(L lex);
}
