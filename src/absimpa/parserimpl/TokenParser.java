/**
 * 
 */
package absimpa.parserimpl;

import java.util.EnumSet;

import absimpa.*;

public class TokenParser<N,C extends Enum<C>>
    extends AbstractParser<N,C>
{
  public TokenParser(C tokenCode) {
    super(EnumSet.of(tokenCode), false);
  }
  protected ParseResult<N> doParse(Lexer<N,C> lex) throws ParseException {    
    N node = lex.next();
    return new ParseResult<N>(node);
  }
  public String toString() {
    C tokenCode = lookahead.iterator().next();
    return tokenCode.toString();
  }
}