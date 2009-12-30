/**
 * 
 */
package absimpa.parserimpl;

import java.util.*;

import absimpa.*;

public class ChoiceParser<N,C extends Enum<C>>
    extends AbstractParser<N,C>
{
  private final List<AbstractParser<N,C>> children;
  /*+******************************************************************/
  public ChoiceParser(List<AbstractParser<N,C>> children,
                      EnumSet<C> lookahead, boolean mayBeEpsilon) {
    super(lookahead, mayBeEpsilon);
    this.children = children;
  }
  /*+******************************************************************/
  @Override
  List<N> doParse(Lexer<N,C> lex) throws ParseException {
    for(AbstractParser<N,C> p : children) {
      ParseResult<N> r = p.parseInternal(lex);
      if( r.isEpsilon() ) {
        continue;
      }
      if( r.notApplicable() ) {
        continue;
      }
      List<N> nodes = new ArrayList<N>(1);
      r.addToNodeList(nodes);
      return nodes;
    }
    
    throw new RuntimeException("this method was obviously called without making"+
                               " sure that this parser's lookahead matches."+
                               " This is wrong. Find the bug!");
  }
  /*+******************************************************************/
  public String toString() {
    StringBuilder sb = new StringBuilder();
    Formatter fmt = new Formatter(sb);
    fmt.format("%s[", getName());
    String sep = "";
    for(AbstractParser<N,C> p : children) {
      fmt.format("%s%s", sep, p.getName());
      sep = "|";
    }
    sb.append("]");
    return sb.toString();
  }
}