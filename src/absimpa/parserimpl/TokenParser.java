/**
 * 
 */
package absimpa.parserimpl;

import java.util.EnumSet;

import absimpa.*;

public class TokenParser<N,C extends Enum<C>,L extends Lexer<C>>
    implements Parser<N,C,L>
{
  private final LeafFactory<N,C,L> leafFactory;
  private final C tokenCode;
  public TokenParser(C tokenCode, LeafFactory<N,C,L> lf) {
    this.leafFactory = lf;
    this.tokenCode = tokenCode;
  }
  public N parse(L lex) throws ParseException {
    C code = lex.current();
    if( code!=tokenCode ) throw lex.parseException(EnumSet.of(tokenCode));

    // N node = leafFactory.create(lex);
    N node = leafFactory.create(lex);
    lex.next();
    return node;
  }
  public String toString() {
    return String.format("%s->%s", tokenCode, leafFactory);
  }
}