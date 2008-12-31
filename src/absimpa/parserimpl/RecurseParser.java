/**
 * 
 */
package absimpa.parserimpl;

import absimpa.*;

public class RecurseParser<N,C extends Enum<C>,L extends Lexer<C>>
    implements ParserI<N,C,L>
{
  private ParserI<N,C,L> child;
  public void setChild(ParserI<N,C,L> child) {
    this.child = child;
  }
  public N parse(L lex) throws ParseException {
    return child.parse(lex);
  }
  public String toString() {
    return "RECURSE";
  }
}