package absimpa;

import java.util.EnumSet;

import absimpa.parserimpl.AbstractParser;

/**
 * <p>
 * The Dragon Book(*) calls <em>first</em> of a grammar rule the set of
 * tokens that will start any text recognized by the rule. The
 * <q>empty token</q>
 * may be an element of this set.
 * 
 * FIXME: find a better name for the class. Since it also stores the
 * resulting parser for a grammar, the name First does not cover its full
 * responsibility.
 * 
 * @see (*)Aho, Sethi, Ullmann: Compiler: Principles, Techniques and Tools
 */
class First<N,C extends Enum<C>> {
  private final EnumSet<C> first;
  public final boolean epsilon;
  private AbstractParser<N,C> parser;
  
  public First(EnumSet<C> first, boolean epsilon) {
    this.first = first;
    this.epsilon = epsilon;
  }
  public EnumSet<C> firstSet() {
    return EnumSet.copyOf(first);
  }
  public void setParser(AbstractParser<N,C> p) {
    this.parser = p;
  }
  public AbstractParser<N,C> getParser() {
    return parser;
  }
}
