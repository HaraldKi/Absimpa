package example;

public interface LeafFactory<N,C extends Enum<C> & LeafFactory<N,C>> {
  N create(TrivialLexer<N,C> lex);
}
