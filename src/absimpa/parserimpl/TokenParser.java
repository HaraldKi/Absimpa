/**
 * 
 */
package absimpa.parserimpl;

import java.util.*;

import absimpa.*;

public class TokenParser<N,C extends Enum<C>>
    extends AbstractParser<N,C>
{
  public TokenParser(C tokenCode) {
    super(EnumSet.of(tokenCode), false);
  }
  List<N> doParse(Lexer<N,C> lex) throws ParseException {    
    N node = lex.next();
    if( node==null ) {
      return Collections.<N>emptyList();
    }
    return Collections.singletonList(node);
  }
  public String toString() {
    C tokenCode = lookahead.iterator().next();
    return tokenCode.toString();
  }
}