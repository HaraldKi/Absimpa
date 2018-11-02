package absimpa;


import java.util.*;

import absimpa.parserimpl.AbstractParser;
import absimpa.parserimpl.RepeatParser;



public class Repeat<N, C extends Enum<C>> extends Grammar<N,C> {
  private final int min;
  private final int max;
  private final Grammar<N,C> child;

  public Repeat(int min, int max, Grammar<N,C> arg) {
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
  @Override
  protected Iterable<Grammar<N,C>> children() {
    return Collections.singletonList(child);
   }
  /*+******************************************************************/
  @Override
  protected AbstractParser<N,C> buildParser(Map<Grammar<N,C>,First<N,C>> firstOf) {
    First<N,C> f = child.first(firstOf);
    EnumSet<C> childLookahead = f.firstSet();
    AbstractParser<N,C> childParser = child.build(firstOf);
    return new RepeatParser<>(childLookahead,
                              childParser, min==0 || f.epsilon, min, max);
  }
  /*+******************************************************************/
  @Override
  protected First<N,C> computeFirst(Map<Grammar<N,C>,First<N,C>> firstOf) {
    First<N,C> f = child.first(firstOf);
    boolean mayBeEpsilon = min==0 || f.epsilon;
    if( mayBeEpsilon != f.epsilon ) {
      return new First<>(f.firstSet(), mayBeEpsilon);
    }
    return f;
  }
  /*+******************************************************************/
  public String getDetail() {
    if( min==0 ) {
      if( max==1 ) return "?";
      if( max==Integer.MAX_VALUE ) return "*";
    }
    if( min==1 ) {
      if( max==Integer.MAX_VALUE ) return "+";
    }
    StringBuilder sb = new StringBuilder();
    sb.append(min).append(',');
    if( max==Integer.MAX_VALUE ) {
      sb.append("*");
    } else {
      sb.append(max);
    }
    return sb.toString();
  }
  /*+******************************************************************/
  @Override
  public String _ruleString() {
    String detail = getDetail();
    String tail;
    if( detail.length()>1 ) {
      tail = "{"+detail+"}";
    } else {
      tail = detail;
    }
    return child.toString()+ tail;
  }
}
