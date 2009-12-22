/**
 * 
 */
package absimpa.parserimpl;

import java.util.*;

import absimpa.*;

/**
 * <p>
 * a purely sequential choice parser that prefers epsilon expansion over
 * later choices of non-epsilon.</p>
 * 
 * @param <N>
 * @param <C>
 * @param <L>
 */
public class ChoiceParser<N,C extends Enum<C>,L extends Lexer<C>>
    implements Parser<N,C,L>
{
  private final List<Parser<N,C,L>> choices;

  public ChoiceParser(List<Parser<N,C,L>> choices) {
    this.choices = choices;
  }
  public ParserResult<N> parse(L lex) throws ParseException {
    for(Parser<N,C,L> p : choices) {
      ParserResult<N> r = p.parse(lex);
      if( r.isEpsilon() || r.isNode() ) {
        return r;
      }
    }
    // TODO: need to collect the expected tokens here
    throw lex.parseException(lookahead());
  }
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("CHOICE{");
    String sep = "";
    for(C c : lookahead()) {
      sb.append(sep).append(c);
      sep = ",";
    }
    sb.append('}');
    return sb.toString();
  }
  
  private Set<C> lookahead() {
    // TODO: need to collect the expected tokens here
    return new HashSet<C>();
  }
  
}