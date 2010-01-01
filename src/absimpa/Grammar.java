package absimpa;


import java.util.*;

import absimpa.parserimpl.AbstractParser;

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
 */

public abstract class Grammar<N, C extends Enum<C>> {
  protected Grammar() {}
  
  private String name = null;
  private NodeFactory<N> nodeFactory = null;
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
  /*+******************************************************************/
  /**
   * <p>wraps {@code this} into a {@link Repeat}.</p>
   */
  public Grammar<N,C> rep(NodeFactory<N> nf, int min, int max) {
    Grammar<N,C> g = new Repeat<N,C>(min, max, this);
    g.setNodeFactory(nf);
    return g;
  }
  /*+******************************************************************/
  /**
   * <p>
   * wraps {@code this} into a {@link Repeat} with {@code min=0} and {code
   * max} really huge.
   * </p>
   */
  public Grammar<N,C> star(NodeFactory<N> nf) {
    return rep(nf, 0, Integer.MAX_VALUE);
  }
  /**
   * <p>
   * wraps {@code this} into a {@link Repeat} with {@code min=0} and {code
   * max} really huge.
   * </p>
   */
  public Grammar<N,C> star() {
    return star(null);
  }
  /*+******************************************************************/
  /**
   * <p>
   * wraps {@code this} into a {@link Repeat} with {@code min=0} and {code
   * max=1}.
   * </p>
   */
  public Grammar<N,C> opt(NodeFactory<N> nf) {
    return rep(nf, 0, 1);
  }
  /**
   * <p>
   * wraps {@code this} into a {@link Repeat} with {@code min=0} and {code
   * max=1}.
   * </p>
   */
  public Grammar<N,C> opt() {
    return opt(null);
  }
  /*+******************************************************************/
  public Grammar<N,C> setNodeFactory(NodeFactory<N> nf) {
    this.nodeFactory = nf;
    return this;
  }
  /*+******************************************************************/
  final AbstractParser<N,C> build(Map<Grammar<N,C>,First<N,C>> firstOf) {
    First<N,C> f = first(firstOf);
    if( f.getParser()!=null ) {
      return f.getParser();
    }
    
    AbstractParser<N,C> p = buildParser(firstOf);
    f.setParser(p);
    p.setName(name);
    p.setNodeFactory(nodeFactory);
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
  final First<N,C> first(Map<Grammar<N,C>,First<N,C>> firstOf) {
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
  protected abstract Iterable<Grammar<N,C>> children();
  protected abstract AbstractParser<N,C> buildParser(Map<Grammar<N,C>,First<N,C>> firstOf);
  protected abstract First<N,C> computeFirst(Map<Grammar<N,C>,First<N,C>> firstOf);
  /**
   * to be implemented by subclasses in order to provide some detail to be
   * put into the toString() output, e.g. the name of the token code for a
   * TokenGrammar or the number of repetitions for a repeat.
   * 
   * @return
   */
  protected abstract String getDetail();
  /* +***************************************************************** */
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
    StringBuilder sb = new StringBuilder(30);
    sb.append(shortClassname());
    String detail = getDetail();
    if( name!=null || detail.length()>0 ) {
      sb.append('(');
      String sep = "";
      if(name!=null) {
        sb.append(name);
        sep = ":"; 
      }
      if( detail.length()>0 ) {
        sb.append(sep).append(detail);
      }
      sb.append(')');
    }
    return sb.toString();
  }
  /* +***************************************************************** */
  private String shortClassname() {
    String className = getClass().getName();
    int p = className.lastIndexOf('.');
    if( p<0 ) return className;
    return className.substring(p+1, className.length());
  }
  /* +***************************************************************** */
  protected void setRecurse(Map<Grammar<N,C>,First<N,C>> firstOf) {
    // only Recurse needs to override.
  }
  /* +***************************************************************** */
  public final String toString() {
    return toString("");
  }
  /*+******************************************************************/
  private String toString(String indent) {
    StringBuilder sb = new StringBuilder();
    
    sb.append(indent).append(getName());
    appendNodeFactory(sb);

    if( this instanceof Recurse<?,?> || this instanceof TokenGrammar<?,?> ) {
      return sb.toString();
    }
    
    sb.append("[\n");
    for(Grammar<N,C> child : children()) {
      sb.append(child.toString("  "+indent));
      sb.append('\n');
    }
    sb.append(indent).append("]");
    return sb.toString();
  }
  /*+******************************************************************/
  private void appendNodeFactory(StringBuilder sb) {
    if( nodeFactory!=null ) {
      sb.append('{').append(nodeFactory).append('}');
    }    
  }
  /*+******************************************************************/
}
