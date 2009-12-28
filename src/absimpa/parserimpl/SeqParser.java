/**
 * 
 */
package absimpa.parserimpl;

import java.util.ArrayList;
import java.util.List;

import absimpa.*;

public class SeqParser<N,C extends Enum<C>>
    implements Parser<N,C>
{
  private final List<Parser<N,C>> children;
  private final NodeFactory<N> nf;
  public SeqParser(NodeFactory<N> nf, List<Parser<N,C>> children) {
    this.nf = nf;
    this.children = children;
  }
  public N parse(Lexer<N,C> lex) throws ParseException {
    List<N> nodes = new ArrayList<N>(children.size());
    int count = 0;
    for(Parser<N,C> p : children) {
      N child = p.parse(lex);
      if( child!=null ) nodes.add(child);
      count += 1;
    }
    if( count==0 ) {
      return null;
    } else {
      return nf.create(nodes);
    }
  }
  public String toString() {
    return String.format("SEQ(%s)", nf);
  }
}