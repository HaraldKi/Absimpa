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
  public ParseResult<N> doParse(Lexer<N,C> lex) throws ParseException {
    boolean sawEpsilon = false;
    for(AbstractParser<N,C> p : children) {
      ParseResult<N> r = p.parseInternal(lex);
      if( r.isEpsilon() ) {
        sawEpsilon = true;
        continue;
      }
      if( r.notApplicable() ) {
        continue;
      }
      return r;
    }
    if( sawEpsilon ) return ParseResult.ISEPSILON();

    throw new RuntimeException("method must have been called without making"+
                               " sure that this parser's lookahead matches."+
                               " This should wrong. Find the bug!");
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