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
  protected String getDetail() {
    return "";
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
    Grammar<N,C> childGrammar = children.get(0);
    First<N,C> childFirst = childGrammar.first(firstOf);
    
    boolean optional = childFirst.epsilon;
    EnumSet<C> firstSet = childFirst.firstSet();
    for(int i=1; i<children.size(); i++) {
      childGrammar = children.get(i);
      childFirst = childGrammar.first(firstOf);
      firstSet.addAll(childFirst.firstSet());
      optional |= childFirst.epsilon;
    }
    return new First<N,C> (firstSet, optional);
  }
  /* +***************************************************************** */
  @Override
  public String _ruleString() {
    if( children.size()>1 ) {
      return Util.join(children, "(", " | ", ")");
    } else if(children.size()==1) {
      return children.get(0).toString();
    } else {
      return "";
    }
  }
}
