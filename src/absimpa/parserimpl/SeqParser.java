/**
 * 
 */
package absimpa.parserimpl;

import java.util.*;

import absimpa.*;

public class SeqParser<N,C extends Enum<C>>
    extends AbstractParser<N,C>
{
  private final List<AbstractParser<N,C>> children;
  /*+******************************************************************/
  public SeqParser(List<AbstractParser<N,C>> children,
                   EnumSet<C> lookahead, boolean mayBeEpsilon) {
    super(lookahead, mayBeEpsilon);
    this.children = children;
  }
  /*+******************************************************************/
  @Override
  List<N> doParse(Lexer<N,C> lex) throws ParseException {
    List<N> nodes = new ArrayList<N>(children.size());

    for(AbstractParser<N,C> p : children) {
      ParseResult<N> r = p.parseInternal(lex);
      if( r.notApplicable() ) {
        throw lex.parseException(p.getLookahead());
      }
      if( !r.isEpsilon() ) { 
        r.addToNodeList(nodes);
      }
    }
    return nodes;
  }
  /*+******************************************************************/
  public String toString() {
    StringBuilder sb = new StringBuilder();
    Formatter fmt = new Formatter(sb);
    fmt.format("%s[", getName());
    String sep = "";
    for(AbstractParser<N,C> p : children) {
      fmt.format("%s%s", sep, p.getName());
      sep = ",";
    }
    sb.append(']');
    return sb.toString();
  }
}