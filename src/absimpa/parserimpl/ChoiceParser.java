/**
 * 
 */
package absimpa.parserimpl;

import java.util.EnumMap;

import absimpa.*;

public class ChoiceParser<N,C extends Enum<C>,L extends Lexer<C>>
    implements Parser<N,C,L>
{
  private final boolean optional;
  private final EnumMap<C,Parser<N,C,L>> choiceMap;

  public ChoiceParser(boolean optional, EnumMap<C,Parser<N,C,L>> choiceMap) {
    this.optional = optional;
    this.choiceMap = choiceMap;
  }
  public N parse(L lex) throws ParseException {
    C code = lex.current();
    Parser<N,C,L> p = choiceMap.get(code);
    if( p!=null ) return p.parse(lex);
    if( optional ) return null;
    throw lex.parseException(choiceMap.keySet());
  }
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("CHOICE{");
    String sep = "";
    for(C c : choiceMap.keySet() ) {
      sb.append(sep).append(c);
      sep = ",";
    }
    sb.append('}');
    return sb.toString();
  }
}