/**
 * 
 */
package absimpa.parserimpl;

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
  public ParserResult<N> parse(L lex) throws ParseException {
    C code = lex.current();
    if( code!=tokenCode ) {
      return ParserResult.NOTAPPLICABLE();
    }
    
    N node = leafFactory.create(lex);
    lex.next();
    return new ParserResult<N>(node);      
  }
  public String toString() {
    return String.format("%s->%s", tokenCode, leafFactory);
  }
}