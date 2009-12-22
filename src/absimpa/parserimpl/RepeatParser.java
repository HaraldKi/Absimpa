/**
 * 
 */
package absimpa.parserimpl;

import java.util.*;

import absimpa.*;

public class RepeatParser<N,C extends Enum<C>,L extends Lexer<C>>
    implements Parser<N,C,L>
{
  private final int min, max;
  private Parser<N,C,L> child;
  private final NodeFactory<N> nf;

  public RepeatParser(NodeFactory<N> nf,Parser<N,C,L> child, int min, int max) {
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

  public ParserResult<N> parse(L lex) throws ParseException {
    List<N> nodes = new ArrayList<N>(min);

    int count = 0;
    while( count<max ) {
      ParserResult<N> r = child.parse(lex);
      count += 1;
      
      if( r.isEpsilon() ) {
        count = min;
        break;
      }
      if( r.notApplicable() ) {
        break;
      }
      
      nodes.add(r.get());
    }
    
    if( count<min ) {
      // TODO: find lookahead tokens for exception
      throw lex.parseException(new HashSet<C>());
    }
    return new ParserResult<N>(nf.create(nodes));
  }
  public String toString() {
    return String.format("REP(%s){%d,%d}", nf, min, max);
  }
}