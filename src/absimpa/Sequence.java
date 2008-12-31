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
    EnumSet<C> firstSet = null;
    boolean optional = true;
    for(Grammar<N,C,L> g : children) {
      Grammar<N,C,L> ga = (Grammar<N,C,L>)g;
      First<N,C,L> f = ga.first(firstOf);
      if( firstSet==null ) {
        firstSet = f.firstSet();
        optional = f.epsilon;
      } else {
        EnumSet<C> otherFirstSet = f.firstSet();
        if( firstSet.removeAll(otherFirstSet) ) {
          throw lookaheadConflict(children, ga, firstOf);
        }
        firstSet.addAll(otherFirstSet);
        optional &= f.epsilon;
      }
      if( !optional ) break;
    }
    return new First<N,C,L>(firstSet, optional);
  }
}
