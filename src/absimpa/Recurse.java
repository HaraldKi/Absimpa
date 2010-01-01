package absimpa;

import java.util.*;

import absimpa.parserimpl.AbstractParser;
import absimpa.parserimpl.RecurseParser;



public class Recurse<N,C extends Enum<C>>
    extends Grammar<N,C>
{
  private Grammar<N,C> child;

  public void setChild(Grammar<N,C> child) {
    this.child = child;
  }
  protected Iterable<Grammar<N,C>> children() {
    return Collections.singletonList(child);
   }
  /* +***************************************************************** */
  protected String getDetail() {
    return "";
  }
  /* +***************************************************************** */
  @Override
  protected AbstractParser<N,C> buildParser(Map<Grammar<N,C>,First<N,C>> firstOf)
  {

    // FIXME: this is very nasty, because I take advantage of the knowledge
    // about how Grammar orchestrates the tree walking. Need to find a
    // cleaner solution.
    First<N,C> f = firstOf.get(this);
    RecurseParser<N,C> myParser =
      new RecurseParser<N,C>(f.firstSet(), f.epsilon);
    f.setParser(myParser);
    myParser.setChild(child.build(firstOf));
    return myParser;
  }
  @Override
  protected First<N,C> computeFirst(Map<Grammar<N,C>,First<N,C>> firstOf)
  {
    return child.computeFirst(firstOf);
  }
  protected void setRecurse(Map<Grammar<N,C>,First<N,C>> firstOf) {
    AbstractParser<N,C> p = firstOf.get(this).getParser();
    RecurseParser<N,C> myParser = (RecurseParser<N,C>)p;
    p = firstOf.get(child).getParser();
    myParser.setChild(p);
  }
}
