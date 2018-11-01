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
    List<N> nodes = new ArrayList<>(children.size());

    for(AbstractParser<N,C> child : children) {
      ParseResult<N> r = child.parseInternal(lex);
      if( r.notApplicable() ) {
        throw lex.parseException(child.getLookahead());
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
    try(Formatter fmt = new Formatter(sb)) {
      fmt.format("%s[", getName());
      String sep = "";
      for(AbstractParser<N,C> p : children) {
        fmt.format("%s%s", sep, p.getName());
        sep = ",";
      }
    }
    sb.append(']');
    return sb.toString();
  }
}