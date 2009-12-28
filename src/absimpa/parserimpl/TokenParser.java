/**
 * 
 */
package absimpa.parserimpl;

import java.util.EnumSet;

import absimpa.*;

public class TokenParser<N,C extends Enum<C>>
    implements Parser<N,C>
{
  private final C tokenCode;

  public TokenParser(C tokenCode) {
    this.tokenCode = tokenCode;
  }
  public N parse(Lexer<N,C> lex) throws ParseException {
    C code = lex.current();
    if( code!=tokenCode ) throw lex.parseException(EnumSet.of(tokenCode));

    // N node = leafFactory.create(lex);
    N node = lex.next();
    return node;
  }
  public String toString() {
    return tokenCode.toString();
  }
}