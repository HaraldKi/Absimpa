package absimpa;


import java.util.*;

import absimpa.parserimpl.SeqParser;

public class Sequence<N,C extends Enum<C>,L extends Lexer<C>>
    extends Grammar<N,C,L>
{
  private final List<Grammar<N,C,L>> children = new ArrayList<Grammar<N,C,L>>(2);
  private final NodeFactory<N> nf;

  public Sequence(NodeFactory<N> nf, Grammar<N,C,L> p) {
    this.nf = nf;
    children.add(p);
  }
  /*+******************************************************************/
  public Sequence<N,C,L> add(Grammar<N,C,L> grammar) {
    children.add(grammar);
    return this;
  }
  /*+******************************************************************/
  protected Iterable<Grammar<N,C,L>> children() {
    return Collections.unmodifiableList(children);
  }
  /* +***************************************************************** */
  protected Parser<N,C,L> buildParser(Map<Grammar<N,C,L>,First<N,C,L>> firstOf) {
    List<Parser<N,C,L>> childParsers =
        new ArrayList<Parser<N,C,L>>(children.size());

    for(Grammar<N,C,L> g : children) {
      Grammar<N,C,L> ga = (Grammar<N,C,L>)g;
      childParsers.add(ga.build(firstOf));
    }
    return new SeqParser<N,C,L>(nf, childParsers);
  }
  /*+******************************************************************/
  @Override
  protected First<N,C,L> computeFirst(Map<Grammar<N,C,L>,First<N,C,L>> firstOf)
  {
    Grammar<N,C,L> g = children.get(0);
    First<N,C,L> f = g.first(firstOf);

    EnumSet<C> firstSet = f.firstSet();
    boolean optional = f.epsilon;

    for(int i=1; i<children.size() && optional; i++) {
      g = children.get(i);
      f = g.first(firstOf);
      EnumSet<C> otherFirstSet = f.firstSet();
      if( firstSet.removeAll(otherFirstSet) ) {
        throw lookaheadConflict(children, g, firstOf);
      }
      firstSet.addAll(otherFirstSet);
      optional &= f.epsilon;
    }
    return new First<N,C,L>(firstSet, optional);
  }
}
