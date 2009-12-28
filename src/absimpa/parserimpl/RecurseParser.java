/**
 * 
 */
package absimpa.parserimpl;

import absimpa.*;

public class RecurseParser<N,C extends Enum<C>>
    implements Parser<N,C>
{
  private Parser<N,C> child;
  public void setChild(Parser<N,C> child) {
    this.child = child;
  }
  public N parse(Lexer<N,C> lex) throws ParseException {
    return child.parse(lex);
  }
  public String toString() {
    return "RECURSE";
  }
}