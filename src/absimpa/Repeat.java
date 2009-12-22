package absimpa;


import java.util.*;

import absimpa.parserimpl.RepeatParser;



public class Repeat<N, C extends Enum<C>,L extends Lexer<C>> extends Grammar<N,C,L> {
  private final int min;
  private final int max;
  private final Grammar<N,C,L> child;
  private final NodeFactory<N> nf;
  
  public Repeat(NodeFactory<N> nf, int min, int max, Grammar<N,C,L> arg) {
    this.nf = nf;
    if( min<0||max<min||max==0 ) {
      String msg =
          String.format("must have 0<=min<=max and max>0, but have "
              +" min=%d, max=%d", min, max);
      throw new IllegalArgumentException(msg);
    }
    this.min = min;
    this.max = max;
    this.child = arg;
  }
  /* +***************************************************************** */
  protected Iterable<Grammar<N,C,L>> children() {
    ArrayList<Grammar<N,C,L>> l = new ArrayList<Grammar<N,C,L>>();
    l.add(child);
    return l;
   }
  /*+******************************************************************/
  protected Parser<N,C,L> buildParser(Map<Grammar<N,C,L>,First<N,C,L>> firstOf) {
    Parser<N,C,L> childParser = child.build(firstOf);
    return new RepeatParser<N,C,L>(nf, childParser, min, max);
  }
  /*+******************************************************************/
  protected First<N,C,L> computeFirst(Map<Grammar<N,C,L>,First<N,C,L>> firstOf) {
    First<N,C,L> f = child.first(firstOf);
    if( (min==0) != f.epsilon ) return new First<N,C,L>(f.firstSet(), min==0);
    else return f;
  }
}
