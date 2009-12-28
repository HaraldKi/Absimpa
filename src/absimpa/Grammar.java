package absimpa;


import java.util.*;

/**
 * <p>
 * The {@code Grammar} describes a language made of sequences of {@code C}
 * objects provided by a {@link absimpa.Lexer}. To create a {@code Grammar},
 * use a {@link GrammarBuilder}. Then call {@link #compile()} to obtain a
 * {@link Parser} for the language described by this {@code Grammar}.
 * </p>
 * 
 * @param <N> is the type of the objects created by the parser generated from
 *        this grammar
 * @param <C> is the type of token codes provided by the lexer that will be
 *        used by this parser
 * @param <L> is the type of {@link absimpa.Lexer} from which the parser
 *        obtains token information
 */

public abstract class Grammar<N, C extends Enum<C>> {
  protected Grammar() {}
  
  private String name = null;
  
  /* +***************************************************************** */
  /**
   * compiles the grammar into a parser to recognize the grammar.
   * 
   * @throws LeftRecursiveException if the grammar is <a
   *         href="http://en.wikipedia.org/wiki/Left_recursion">left
   *         recursive</a>.
   * @throws LookaheadConflictException if either a {@link Choice} or a
   *         {@link Sequence} encountered during compilation has a lookahead
   *         conflict.
   */
  public final Parser<N,C> compile() {
    Map<Grammar<N,C>,First<N,C>> firstOf =
        new HashMap<Grammar<N,C>,First<N,C>>();

    Parser<N,C> result = build(firstOf);
    fillRecursives(firstOf, new HashSet<Grammar<N,C>>());
    return result;
  }
  /* +***************************************************************** */
  protected final Parser<N,C> build(Map<Grammar<N,C>,First<N,C>> firstOf) {
    First<N,C> f = first(firstOf);
    if( f.getParser()!=null ) return f.getParser();
    Parser<N,C> p = buildParser(firstOf);
    f.setParser(p);
    return p;
  }
  /*+******************************************************************/
  private final void fillRecursives(Map<Grammar<N,C>,First<N,C>> firstOf,
                                    Set<Grammar<N,C>> done) {
    for(Grammar<N,C> g : children()) {
      g.setRecurse(firstOf);
      if( done.contains(g) ) continue;
      done.add(g);
      g.fillRecursives(firstOf, done);
    }
  }
  /* +***************************************************************** */
  protected final First<N,C> first(Map<Grammar<N,C>,First<N,C>> firstOf) {
    if( firstOf.containsKey(this) ) {
      First<N,C> myFirst = firstOf.get(this);
      if( myFirst!=null ) return myFirst;
      // FIXME: message with loop elements would be nice.
      String msg = 
        String.format("grammar %s starts a left recursive loop",
                      this);
      throw new LeftRecursiveException(msg);
    }

    firstOf.put(this, null);
    First<N,C> f = computeFirst(firstOf);
    firstOf.put(this, f);
    return f;
  }
  /* +***************************************************************** */
  protected abstract Parser<N,C> buildParser(Map<Grammar<N,C>,First<N,C>> firstOf);
  protected abstract First<N,C> computeFirst(Map<Grammar<N,C>,First<N,C>> firstOf);
  /* +***************************************************************** */
  protected LookaheadConflictException lookaheadConflict(List<Grammar<N,C>> children,
                                                         Grammar<N,C> g,
                                                         Map<Grammar<N,C>,First<N,C>> firstOf)
  {
    EnumSet<C> gFirstSet = g.first(firstOf).firstSet();
    for(int i = children.indexOf(g); i>=0; --i) {
      Grammar<N,C> other = (Grammar<N,C>)(children.get(i));
      EnumSet<C> otherFirstSet = other.first(firstOf).firstSet();
      if( !otherFirstSet.removeAll(gFirstSet) ) continue;
      otherFirstSet = other.first(firstOf).firstSet();
      otherFirstSet.retainAll(gFirstSet);
      return new LookaheadConflictException(otherFirstSet, g, other);
    }
    throw new RuntimeException("this should never happen");
  }
  /*+******************************************************************/
  /**
   * used by {@link #toString} only, not needed for the function of the
   * grammar.
   */
  public Grammar<N,C> setName(String name) {
    this.name = name;
    return this;
  }
  /* +***************************************************************** */
  public String getName() {
    if( name!=null ) return name;
    return shortClassname();
  }
  /* +***************************************************************** */
  /**
   * returns a name and, potentially, the names of direct children.
   */
  public String toString() {
    StringBuilder sb = new StringBuilder(getName());
    char sep = '[';
    for(Grammar<N,C> g : children()) {
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
  protected abstract Iterable<Grammar<N,C>> children();
  protected void setRecurse(Map<Grammar<N,C>,First<N,C>> firstOf) {
    // only Recurse needs to override.
  }
  /* +***************************************************************** */
}
