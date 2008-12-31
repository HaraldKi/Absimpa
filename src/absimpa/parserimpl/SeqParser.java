/**
 * 
 */
package absimpa.parserimpl;

import java.util.ArrayList;
import java.util.List;

import absimpa.*;

public class SeqParser<N,C extends Enum<C>,L extends Lexer<C>>
    implements ParserI<N,C,L>
{
  private final List<ParserI<N,C,L>> children;
  private final NodeFactory<N> nf;
  public SeqParser(NodeFactory<N> nf, List<ParserI<N,C,L>> children) {
    this.nf = nf;
    this.children = children;
  }
  public N parse(L lex) throws ParseException {
    List<N> nodes = new ArrayList<N>(children.size());
    for(ParserI<N,C,L> p : children) {
      N child = p.parse(lex);
      if( child!=null ) nodes.add(child);
    }
    return nf.create(nodes);
  }
  public String toString() {
    return String.format("SEQ(%s)", nf);
  }
}