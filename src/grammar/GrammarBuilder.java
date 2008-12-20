package grammar;

import lexer.Lexer;


public class GrammarBuilder<N,C extends Enum<C>,L extends Lexer<C>> {
  private final NodeFactory<N,C> defaultFactory;
  /*+******************************************************************/
  public GrammarBuilder(NodeFactory<N,C> defaultFactory) {
    this.defaultFactory = defaultFactory;
  }
  /*+******************************************************************/
  public TokenGrammar<N,C,L> token(LeafFactory<N,C,L> fac, C code) {
    return new TokenGrammar<N,C,L>(fac, code);
  }
  /* +***************************************************************** */
  public Sequence<N,C,L> seq(NodeFactory<N,C> inner, Grammar<N,C,L> g) {
    return new Sequence<N,C,L>(inner, g);
  }
  public Sequence<N,C,L> seq(Grammar<N,C,L> g) {
    return new Sequence<N,C,L>(defaultFactory, g);
  }
  /*+******************************************************************/
  public Repeat<N,C,L> repeat(NodeFactory<N,C> inner, Grammar<N,C,L> grammar,
                            int min, int max)
  {
    return new Repeat<N,C,L>(inner, min, max, grammar);
  }
  public Repeat<N,C,L> repeat(Grammar<N,C,L> grammar, int min, int max) {
    return new Repeat<N,C,L>(defaultFactory, min, max, grammar);
  }
  public Repeat<N,C,L> star(Grammar<N,C,L> grammar) {
    return repeat(grammar, 0, Integer.MAX_VALUE);
  }
  public Repeat<N,C,L> star(NodeFactory<N,C> nf, Grammar<N,C,L> grammar) {
    return repeat(nf, grammar, 0, Integer.MAX_VALUE);
  }
  /*+******************************************************************/
  /*+******************************************************************/
  public Choice<N,C,L> choice(Grammar<N,C,L> grammar) {
    return new Choice<N,C,L>(grammar);
  }
  /*+******************************************************************/
}
