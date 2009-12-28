package absimpa;

import java.util.*;

import absimpa.parserimpl.TokenParser;



public class TokenGrammar<N,C extends Enum<C>>
    extends Grammar<N,C>
{
  private final C code;

  public TokenGrammar(C code) {
    this.code = code;
  }
  @Override
  protected Iterable<Grammar<N,C>> children()
  {
    return new ArrayList<Grammar<N,C>>(0);
  }
  protected Parser<N,C> buildParser(Map<Grammar<N,C>,First<N,C>> firstOf) {
    return new TokenParser<N,C>(code);
  }
  @Override
  protected First<N,C> computeFirst(Map<Grammar<N,C>,First<N,C>> firstOf)
  {
    EnumSet<C> firstSet = EnumSet.of(code);
    return new First<N,C>(firstSet, false);
  }
  public String toString() {
    return String.format("%s{%s}", getName(), code);
  }
}
