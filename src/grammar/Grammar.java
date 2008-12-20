package grammar;


import java.util.*;

import lexer.Lexer;


public abstract class Grammar<N, C extends Enum<C>,L extends Lexer<C>> {
  private String name = null;
  /* +***************************************************************** */
  public final Parser<N,C,L> compile() {
    Map<Grammar<N,C,L>,First<N,C,L>> firstOf =
        new HashMap<Grammar<N,C,L>,First<N,C,L>>();

    Parser<N,C,L> result = build(firstOf);
    fillRecursives(firstOf, new HashSet<Grammar<N,C,L>>());
    return result;
  }
  /* +***************************************************************** */
  public Grammar<N,C,L> setName(String name) {
    this.name = name;
    return this;
  }
  /* +***************************************************************** */
  public String getName() {
    if( name!=null ) return name;
    return shortClassname();
  }
  /* +***************************************************************** */
  public String toString() {
    StringBuilder sb = new StringBuilder(getName());
    char sep = '[';
    for(Grammar<N,C,L> g : children()) {
      sb.append(sep).append(g.getName());
      sep = ',';
    }
    if( sep==',') sb.append(']');
    return sb.toString();
  }
  /* +***************************************************************** */
  protected String shortClassname() {
    String className = getClass().getName();
    int p = className.lastIndexOf('.');
    if( p<0 ) return className;
    return className.substring(p+1, className.length());
  }
  /* +***************************************************************** */
  private final void fillRecursives(Map<Grammar<N,C,L>,First<N,C,L>> firstOf,
                                    Set<Grammar<N,C,L>> done) {
    for(Grammar<N,C,L> g : children()) {
      g.setRecurse(firstOf);
      if( done.contains(g) ) continue;
      done.add(g);
      g.fillRecursives(firstOf, done);
    }
  }
  /* +***************************************************************** */
  protected abstract Iterable<Grammar<N,C,L>> children();
  protected void setRecurse(Map<Grammar<N,C,L>,First<N,C,L>> firstOf) {
    // only Recurse needs to override.
  }
  /* +***************************************************************** */
  protected final First<N,C,L> first(Map<Grammar<N,C,L>,First<N,C,L>> firstOf) {
    if( firstOf.containsKey(this) ) {
      First<N,C,L> myFirst = firstOf.get(this);
      if( myFirst!=null ) return myFirst;
      // FIXME: message with loop elements would be nice.
      String msg = 
        String.format("grammar %s starts a left recursive loop",
                      this);
      throw new LeftRecursiveException(msg);
    }
    firstOf.put(this, null);
    First<N,C,L> f = computeFirst(firstOf);
    firstOf.put(this, f);
    return f;
  }
  /* +***************************************************************** */
  protected final Parser<N,C,L> build(Map<Grammar<N,C,L>,First<N,C,L>> firstOf) {
    First<N,C,L> f = first(firstOf);
    if( f.getParser()!=null ) return f.getParser();
    Parser<N,C,L> p = buildParser(firstOf);
    f.setParser(p);
    return p;
  }
  /*+******************************************************************/
  protected abstract First<N,C,L> computeFirst(Map<Grammar<N,C,L>,First<N,C,L>> firstOf);
  protected abstract Parser<N,C,L> buildParser(Map<Grammar<N,C,L>,First<N,C,L>> firstOf);
  /* +***************************************************************** */
  protected LookaheadConflictException lookaheadConflict(List<Grammar<N,C,L>> children,
                                                         Grammar<N,C,L> g,
                                                         Map<Grammar<N,C,L>,First<N,C,L>> firstOf)
  {
    EnumSet<C> gFirstSet = g.first(firstOf).firstSet();
    for(int i = children.indexOf(g); i>0; --i) {
      Grammar<N,C,L> other = (Grammar<N,C,L>)(children.get(i));
      EnumSet<C> otherFirstSet = other.first(firstOf).firstSet();
      if( !otherFirstSet.removeAll(gFirstSet) ) continue;
      otherFirstSet = other.first(firstOf).firstSet();
      otherFirstSet.retainAll(gFirstSet);
      return new LookaheadConflictException(otherFirstSet, g, other);
    }
    throw new RuntimeException("this should never happen");
  }
  /*+******************************************************************/
}
