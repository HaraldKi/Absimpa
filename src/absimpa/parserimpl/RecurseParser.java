/**
 * 
 */
package absimpa.parserimpl;

import java.util.EnumSet;

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
  public ParseResult<N> doParse(Lexer<N,C> lex) throws ParseException {
    return child.parseInternal(lex);
  }
  public String toString() {
    return String.format("%s[%s]", getName(), child.getName());
  }

}