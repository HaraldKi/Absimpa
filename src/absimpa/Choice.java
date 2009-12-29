package absimpa;
import java.util.*;

import absimpa.parserimpl.AbstractParser;
import absimpa.parserimpl.ChoiceParser;


public class Choice<N,C extends Enum<C>>
    extends Grammar<N,C>
{
  private final List<Grammar<N,C>> children;
  // private final Grammar<N,C> firstOptional;

  public Choice(Grammar<N,C> g) {
    children = new ArrayList<Grammar<N,C>>();
    children.add(g);
  }
  /* +***************************************************************** */
  public Choice<N,C> or(Grammar<N,C> g) {
    children.add(g);
    return this;
  }
  /* +***************************************************************** */
  protected Iterable<Grammar<N,C>> children() {
    return Collections.unmodifiableList(children);
  }
  /* +***************************************************************** */
  protected AbstractParser<N,C> buildParser(
                                            Map<Grammar<N,C>,First<N,C>> firstOf)
  {
    List<AbstractParser<N,C>> childParsers =
        new ArrayList<AbstractParser<N,C>>(children.size());

    for(Grammar<N,C> g : children) {
      childParsers.add(g.build(firstOf));
    }
    First<N,C> myFirst = first(firstOf);
    return new ChoiceParser<N,C>(childParsers, myFirst.firstSet(),
        myFirst.epsilon);
  }
  /* +***************************************************************** */
  protected First<N,C> computeFirst(Map<Grammar<N,C>,First<N,C>> firstOf) {
    boolean optional = false;
    EnumSet<C> firstSet = null;

    for(Grammar<N,C> ga: children) {
      First<N,C> f = ga.first(firstOf);
      if( firstSet==null ) {
        firstSet = f.firstSet();
      } else {
        EnumSet<C> otherFirst = f.firstSet();
        if( firstSet.removeAll(otherFirst) ) {
          throw lookaheadConflict(children, ga, firstOf);
        }
        firstSet.addAll(f.firstSet());
      }
      optional |= f.epsilon;
    }
    return new First<N,C> (firstSet, optional);
  }
  /* +***************************************************************** */
}
