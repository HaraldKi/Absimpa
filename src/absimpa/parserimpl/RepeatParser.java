/**
 * 
 */
package absimpa.parserimpl;

import java.util.*;

import absimpa.*;

public class RepeatParser<N,C extends Enum<C>>
    extends AbstractParser<N,C>
{
  private final int min, max;
  private AbstractParser<N,C> child;

  public RepeatParser(EnumSet<C> childLookahead,
                      AbstractParser<N,C> child, boolean mayBeEpsilon,
                      int min, int max) {
    super(childLookahead, mayBeEpsilon);
    if( min<0||max<min||max==0 ) {
      String msg =
          String.format("must have 0<=min<=max and max>0, but have "
              +" min=%d, max=%d", min, max);
      throw new IllegalArgumentException(msg);
    }
    this.min = min;
    this.max = max;
    this.child = child;
  }
  /*+******************************************************************/
  List<N> doParse(Lexer<N,C> lex) throws ParseException {
    List<N> nodes = new ArrayList<>(min);

    int count = 0;
    while( count<max ) {
      ParseResult<N> r = child.parseInternal(lex);
      if( r.isEpsilon() ) {
        break;
      }
      if( r.notApplicable() ) {
        if( count<min ) {
          throw lex.parseException(lookahead);
        }
        break;
      }
      r.addToNodeList(nodes);
      count += 1;
    }
    return nodes;
  }
  /*+******************************************************************/
  public String toString() {
    return String.format("REP{%d,%d,%s}", min, max, child.getName());
  }
}