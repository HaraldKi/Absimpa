package absimpa;


import java.util.*;

import absimpa.parserimpl.AbstractParser;
import absimpa.parserimpl.SeqParser;

/**
 * is a grammar to recognize a sequence of child grammars. In BNF notation
 * this would typically be written like the right side of</p>
 * 
 * <pre>
 * A -> B C D E</pre>
 * <p>
 * The parser created from this grammar parses on a first come first serve
 * basis, meaning that a sequence like {@code A? A B} can not parse the token
 * sequence {@code A B}, due to the fact that the optional {@code A} matches
 * immediately, leaving only the {@code B} of the input to be parsed, which
 * does not match on the 2nd {@code A} of the rule. But notice that the rule
 * {@code A A? B} is equivalent and can match {@code A B} just fine.
 * </p>
 */
public class Sequence<N,C extends Enum<C>>
    extends Grammar<N,C>
{
  private final List<Grammar<N,C>> children = new ArrayList<Grammar<N,C>>(2);
  private NodeFactory<N> nf;

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
  public Sequence<N,C> setNodeFactory(NodeFactory<N> factory) {
    this.nf = factory;
    return this;
  }
  /* +***************************************************************** */
  protected AbstractParser<N,C> buildParser(Map<Grammar<N,C>,First<N,C>> firstOf) {
    List<AbstractParser<N,C>> childParsers =
        new ArrayList<AbstractParser<N,C>>(children.size());

    for(Grammar<N,C> g : children) {
      childParsers.add(g.build(firstOf));
    }
    First<N,C> myFirst = first(firstOf);
    return new SeqParser<N,C>(nf, childParsers, myFirst.firstSet(),
        myFirst.epsilon);
  }
  /*+******************************************************************/
  @Override
  protected First<N,C> computeFirst(Map<Grammar<N,C>,First<N,C>> firstOf)
  {
    Grammar<N,C> child = children.get(0);
    First<N,C> childFirst = child.first(firstOf);

    EnumSet<C> firstSet = childFirst.firstSet();
    boolean optional = childFirst.epsilon;

    for(int i=1; i<children.size() && optional; i++) {
      child = children.get(i);
      childFirst = child.first(firstOf);
      EnumSet<C> otherFirstSet = childFirst.firstSet();
//      if( firstSet.removeAll(otherFirstSet) ) {
        // FIXME: don't want any lookahead conflict any more
  //      throw lookaheadConflict(children, child, firstOf);
    //  }
      firstSet.addAll(otherFirstSet);
      optional &= childFirst.epsilon;
    }
    return new First<N,C>(firstSet, optional);
  }
}
