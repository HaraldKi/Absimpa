package absimpa;

import java.util.*;

import absimpa.lexer.LexerInfo;
import absimpa.parserimpl.AbstractParser;
import absimpa.parserimpl.TokenParser;

/**
 * <p>a grammar to recognize a single token.</p>
 * @param <C> is the type of token recognized.
 * @param <N> is the type of object created from the token
 */
public class TokenGrammar<N,C extends Enum<C>>
    extends Grammar<N,C>
{
  private final C code;

  public TokenGrammar(C code) {
    this.code = code;
  }
  @Override
  protected Iterable<Grammar<N,C>> children()
  {
    return Collections.emptyList();
  }
  protected AbstractParser<N,C> buildParser(Map<Grammar<N,C>,First<N,C>> firstOf) {
    return new TokenParser<N,C>(code);
  }
  @Override
  protected First<N,C> computeFirst(Map<Grammar<N,C>,First<N,C>> firstOf)
  {
    EnumSet<C> firstSet = EnumSet.of(code);
    return new First<N,C>(firstSet, false);
  }
  public String getDetail() {
    return code.toString();
  }
  public String _ruleString() {
    String name = getName();
    String representation = null;
    if( code instanceof LexerInfo<?> ) {
      representation = "\""+((LexerInfo<?>)code).getRegex()+'"';
    } else {
      representation = code.toString();
    }
    if( name==null ) {
      return representation;
    } else {
      return name + "(" + representation +")";
    }
  }
}
