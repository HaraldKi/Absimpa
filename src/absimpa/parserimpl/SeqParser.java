/**
 * 
 */
package absimpa.parserimpl;

import java.util.*;

import absimpa.*;

public class SeqParser<N,C extends Enum<C>,L extends Lexer<C>>
    implements Parser<N,C,L>
{
  private final List<Parser<N,C,L>> children;
  private final NodeFactory<N> nf;
  
  public SeqParser(NodeFactory<N> nf, List<Parser<N,C,L>> children) {
    this.nf = nf;
    this.children = children;
  }
  public ParserResult<N> parse(L lex) throws ParseException {
    List<N> nodes = new ArrayList<N>(children.size());
    
    for(Parser<N,C,L> p : children) {
      ParserResult<N> child = p.parse(lex);
      if( child.isEpsilon() ) continue;
      if( child.isNode() ) {
        nodes.add(child.get());
        continue;
      }
      // TODO: compute the right lookahead for p
      throw lex.parseException(new HashSet<C>());
    }
    return new ParserResult<N>(nf.create(nodes));
  }
  public String toString() {
    return String.format("SEQ(%s)", nf);
  }
}