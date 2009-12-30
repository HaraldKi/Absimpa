/**
 * 
 */
package absimpa.parserimpl;

import java.util.*;

import absimpa.*;

public class RecurseParser<N,C extends Enum<C>>
    extends AbstractParser<N,C>
{
  private AbstractParser<N,C> child;
  /*+******************************************************************/
  public RecurseParser(EnumSet<C> lookahead,
                       boolean mayBeEpsilon) {
    super(lookahead, mayBeEpsilon);
  }
  /*+******************************************************************/
  public void setChild(AbstractParser<N,C> child)
  {
    this.child = child;
  }
  @Override
  List<N> doParse(Lexer<N,C> lex) throws ParseException {
    List<N> nodes = new ArrayList<N>(1);
    ParseResult<N> pr = child.parseInternal(lex);
    pr.addToNodeList(nodes);
    return nodes;
  }
  public String toString() {
    return String.format("%s[%s]", getName(), child.getName());
  }

}