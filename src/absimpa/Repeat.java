package absimpa;


import java.util.*;

import absimpa.parserimpl.AbstractParser;
import absimpa.parserimpl.RepeatParser;



public class Repeat<N, C extends Enum<C>> extends Grammar<N,C> {
  private final int min;
  private final int max;
  private final Grammar<N,C> child;
  private final NodeFactory<N> nf;
  
  public Repeat(NodeFactory<N> nf, int min, int max, Grammar<N,C> arg) {
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
  protected Iterable<Grammar<N,C>> children() {
    ArrayList<Grammar<N,C>> l = new ArrayList<Grammar<N,C>>();
    l.add(child);
    return l;
   }
  /*+******************************************************************/
  protected AbstractParser<N,C> buildParser(Map<Grammar<N,C>,First<N,C>> firstOf) {
    First<N,C> f = child.first(firstOf);
    EnumSet<C> childLookahead = f.firstSet();
    AbstractParser<N,C> childParser = child.build(firstOf);
    return new RepeatParser<N,C>(childLookahead, nf, 
        childParser, min==0 || f.epsilon, min, max);
  }
  /*+******************************************************************/
  protected First<N,C> computeFirst(Map<Grammar<N,C>,First<N,C>> firstOf) {
    First<N,C> f = child.first(firstOf);
    boolean mayBeEpsilon = min==0 || f.epsilon; 
    if( mayBeEpsilon != f.epsilon ) {
      return new First<N,C>(f.firstSet(), mayBeEpsilon);
    }
    else return f;
  }
}
