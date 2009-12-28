package absimpa;


import java.util.*;

import absimpa.parserimpl.SeqParser;

public class Sequence<N,C extends Enum<C>>
    extends Grammar<N,C>
{
  private final List<Grammar<N,C>> children = new ArrayList<Grammar<N,C>>(2);
  private final NodeFactory<N> nf;

  public Sequence(NodeFactory<N> nf, Grammar<N,C> p) {
    this.nf = nf;
    children.add(p);
  }
  /*+******************************************************************/
  public Sequence<N,C> add(Grammar<N,C> grammar) {
    children.add(grammar);
    return this;
  }
  /*+******************************************************************/
  protected Iterable<Grammar<N,C>> children() {
    return Collections.unmodifiableList(children);
  }
  /* +***************************************************************** */
  protected Parser<N,C> buildParser(Map<Grammar<N,C>,First<N,C>> firstOf) {
    List<Parser<N,C>> childParsers =
        new ArrayList<Parser<N,C>>(children.size());

    for(Grammar<N,C> g : children) {
      Grammar<N,C> ga = (Grammar<N,C>)g;
      childParsers.add(ga.build(firstOf));
    }
    return new SeqParser<N,C>(nf, childParsers);
  }
  /*+******************************************************************/
  @Override
  protected First<N,C> computeFirst(Map<Grammar<N,C>,First<N,C>> firstOf)
  {
    Grammar<N,C> g = children.get(0);
    First<N,C> f = g.first(firstOf);

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
    return new First<N,C>(firstSet, optional);
  }
}
