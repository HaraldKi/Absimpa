package example;

public interface LeafXFactory<N,C extends Enum<C> & LeafXFactory<N,C>> {
  N create(TrivialLexer<N,C> lex);
}
