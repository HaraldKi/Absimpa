package absimpa;

import java.util.*;



public class TokenGrammar<N,C extends Enum<C>,L extends Lexer<C>>
    extends Grammar<N,C,L>
{
  private final C code;
  private final LeafFactory<N,C,L> lf;

  public TokenGrammar(LeafFactory<N,C,L> lf, C code) {
    this.lf = lf;
    this.code = code;
  }
  @Override
  protected Iterable<Grammar<N,C,L>> children()
  {
    return new ArrayList<Grammar<N,C,L>>(0);
  }
  protected Parser<N,C,L> buildParser(Map<Grammar<N,C,L>,First<N,C,L>> firstOf) {
    return new Parser.TokenParser<N,C,L>(code, lf);
  }
  @Override
  protected First<N,C,L> computeFirst(Map<Grammar<N,C,L>,First<N,C,L>> firstOf)
  {
    EnumSet<C> firstSet = EnumSet.of(code);
    return new First<N,C,L>(firstSet, false);
  }
  public String toString() {
    return String.format("%s{%s}", getName(), code);
  }
}
