/**
 * 
 */
package absimpa.parserimpl;

import absimpa.*;

public class RecurseParser<N,C extends Enum<C>,L extends Lexer<C>>
    implements Parser<N,C,L>
{
  private Parser<N,C,L> child;
  public void setChild(Parser<N,C,L> child) {
    this.child = child;
  }
  public N parse(L lex) throws ParseException {
    return child.parse(lex);
  }
  public String toString() {
    return "RECURSE";
  }
}