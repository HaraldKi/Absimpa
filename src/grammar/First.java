package grammar;

import java.util.EnumSet;

import lexer.Lexer;


class First<N,C extends Enum<C>,L extends Lexer<C>> {
  private final EnumSet<C> first;
  public final boolean epsilon;
  private Parser<N,C,L> parser;
  
  public First(EnumSet<C> first, boolean epsilon) {
    this.first = first;
    this.epsilon = epsilon;
  }
  public EnumSet<C> firstSet() {
    return EnumSet.copyOf(first);
  }
  public void setParser(Parser<N,C,L> p) {
    this.parser = p;
  }
  public Parser<N,C,L> getParser() {
    return parser;
  }
}
