package absimpa;

import java.util.*;

import absimpa.parserimpl.RecurseParser;



public class Recurse<N,C extends Enum<C>>
    extends Grammar<N,C>
{
  private Grammar<N,C> child;

  public void setChild(Grammar<N,C> child) {
    this.child = child;
  }
  protected Iterable<Grammar<N,C>> children() {
    List<Grammar<N,C>> l = new ArrayList<Grammar<N,C>>();
    l.add(child);
    return l;
  }
  /* +***************************************************************** */
  @Override
  protected Parser<N,C> buildParser(Map<Grammar<N,C>,First<N,C>> firstOf)
  {

    // FIXME: this is very nasty, because I take advantage of the knowledge
    // about how Grammar orchestrates the tree walking. Need to find a
    // cleaner solution.
    First<N,C> f = firstOf.get(this);
    RecurseParser<N,C> myParser = new RecurseParser<N,C>();
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
    Parser<N,C> p = firstOf.get(this).getParser();
    RecurseParser<N,C> myParser = (RecurseParser<N,C>)p;
    p = firstOf.get(child).getParser();
    myParser.setChild(p);
  }
}
