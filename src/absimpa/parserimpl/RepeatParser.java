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
  private final NodeFactory<N> nf;

  public RepeatParser(EnumSet<C> childLookahead, NodeFactory<N> nf,
                      AbstractParser<N,C> child, boolean mayBeEpsilon,
                      int min, int max) {
    super(childLookahead, mayBeEpsilon);
    if( min<0||max<min||max==0 ) {
      String msg =
          String.format("must have 0<=min<=max and max>0, but have "
              +" min=%d, max=%d", min, max);
      throw new IllegalArgumentException(msg);
    }
    this.nf = nf;
    this.min = min;
    this.max = max;
    this.child = child;
  }
  /*+******************************************************************/
  protected final ParseResult<N> doParse(Lexer<N,C> lex) throws ParseException {
    List<N> nodes = new ArrayList<N>(min);

    int count = 0;
    while( count<max ) {
      ParseResult<N> r = child.parseInternal(lex);
      if( r.isEpsilon() ) {
        break;
      }
      if( r.notApplicable() ) {
        if( count<min ) {
          throw lex.parseException(lookahead);
        } else {
          break;
        }
      }
      N node = r.getNode();
      if( node!=null ) nodes.add(node);
      count += 1;
    }
    return new ParseResult<N>(nf.create(nodes));
  }
  /*+******************************************************************/
  public String toString() {
    return String.format("REP(%s){%d,%d,%s}", nf, min, max, child.getName());
  }
}