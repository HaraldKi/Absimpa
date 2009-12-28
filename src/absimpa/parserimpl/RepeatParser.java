/**
 * 
 */
package absimpa.parserimpl;

import java.util.*;

import absimpa.*;

public class RepeatParser<N,C extends Enum<C>>
    implements Parser<N,C>
{
  private final int min, max;
  private final EnumSet<C> childLookahead;
  private Parser<N,C> child;
  private final NodeFactory<N> nf;

  public RepeatParser(EnumSet<C> childLookahead, NodeFactory<N> nf,
               Parser<N,C> child, int min, int max) {
    if( min<0||max<min||max==0 ) {
      String msg =
          String.format("must have 0<=min<=max and max>0, but have "
              +" min=%d, max=%d", min, max);
      throw new IllegalArgumentException(msg);
    }
    this.nf = nf;
    this.min = min;
    this.max = max;
    this.childLookahead = childLookahead;
    this.child = child;
  }

  public N parse(Lexer<N,C> lex) throws ParseException {
    List<N> nodes = new ArrayList<N>(min);

    int count = 0;
    C code = lex.current();
    while( count<min||(count<max&&childLookahead.contains(code)) ) {
      // REMINDER: child may be an optional node that continuously
      // returns null due to a lookahead mismatch. In that case we loop
      // until min without progress in reading input. Thats ok, because
      // most of the time min is either 0 or 1, or the child is not
      // optional.
      N node = child.parse(lex);
      if( node!=null ) nodes.add(node);
      count += 1;
      code = lex.current();
    }
    if( count==0 ) {
      return null;
    } else {
      return nf.create(nodes);
    }
  }
  public String toString() {
    return String.format("REP(%s){%d,%d, la=%s}", nf, min, max, childLookahead);
  }
}