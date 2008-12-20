package grammar;

import java.util.*;



public class Recurse<N,C extends Enum<C>,L extends Lexer<C>>
    extends Grammar<N,C,L>
{
  private Grammar<N,C,L> child;

  public void setChild(Grammar<N,C,L> child) {
    this.child = child;
  }
  protected Iterable<Grammar<N,C,L>> children() {
    List<Grammar<N,C,L>> l = new ArrayList<Grammar<N,C,L>>();
    l.add(child);
    return l;
  }
  /* +***************************************************************** */
  @Override
  protected Parser<N,C,L> buildParser(Map<Grammar<N,C,L>,First<N,C,L>> firstOf)
  {

    // FIXME: this is very nasty, because I take advantage of the knowledge
    // about how Grammar orchestrates the tree walking. Need to find a
    // cleaner solution.
    First<N,C,L> f = firstOf.get(this);
    Parser.RecurseParser<N,C,L> myParser = new Parser.RecurseParser<N,C,L>();
    f.setParser(myParser);
    myParser.setChild(child.build(firstOf));
    return myParser;
  }
  @Override
  protected First<N,C,L> computeFirst(Map<Grammar<N,C,L>,First<N,C,L>> firstOf)
  {
    return child.computeFirst(firstOf);
  }
  protected void setRecurse(Map<Grammar<N,C,L>,First<N,C,L>> firstOf) {
    Parser<N,C,L> p = firstOf.get(this).getParser();
    Parser.RecurseParser<N,C,L> myParser = (Parser.RecurseParser<N,C,L>)p;
    p = firstOf.get(child).getParser();
    myParser.setChild(p);
  }
}
