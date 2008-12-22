package example;


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import absimpa.Lexer;
import absimpa.ParseException;

/**
 * <p>
 * provides a trival implementation of a {@link absimpa.Lexer} which analyzes
 * a string by trying out regular expressions for tokens until a match is
 * found. This is not intended for productive use. It is merely an example.
 * </p>
 * 
 * <p>
 * This lexer is set up by specifying a list of pairs (regex, C), where
 * <code>C</code> is some enumeration type, the generic parameter of this
 * class. To analyze in input string, the lexer tries to match each of the
 * regular expressions at the beginning of the input string. If it finds a
 * match, the associated <code>C</code> represents the current token code
 * provided to the parser. If {@link #next} is called, the matching prefix of
 * the input is discarded and the lexer tries again.
 * </p>
 * 
 * <p>
 * If no match can be found, the first character of the input is silently
 * discarded and the lexer starts over trying to match the regular
 * expressions. Consequently, input that cannot be matched is silently
 * discarded.
 * </p>
 * 
 * @param <C> is an enumeration and describes the token codes provided to the
 *        parser
 */
public class TrivialLexer<C extends Enum<C>> implements Lexer<C> {
  private final List<TokenInfo<C>> tokenInfos = new ArrayList<TokenInfo<C>>();
  private final Token<C> eofToken;

  private final StringBuilder restText = new StringBuilder();  
  private Token<C> currentToken = null;

  private int line;
  private int column;
  /*+******************************************************************/
  /**
   * <p>
   * creates a <code>TrivialLexer</code> to return <code>eofCode</code>
   * when the end of input is encountered.
   * </p>
   */
  public TrivialLexer(C eofCode) {
    eofToken = new Token<C>("", eofCode);
  }
  /*+******************************************************************/
  /**
   * <p>
   * resets the lexer and initializes it to analyze the given
   * <code>text</code>. Internally, {@link #next} is called to prepare the
   * first token as the current token.
   * </p>
   */
  public void initAnalysis(CharSequence text) {    
    restText.setLength(0);
    restText.append(text);
    line = 1;
    column = 1;
    next();
  }
  /*+******************************************************************/
  public ParseException parseException(Set<C> expectedTokens) {
    ParseException p = 
      new ParseException(expectedTokens, currentToken.getCode());
    p.setTokenText(currentToken.getText());
    p.setColumn(column);
    p.setLine(line);
    currentToken = null;
    return p;
  }
  /*+******************************************************************/
  /**
   * <p>
   * adds a mapping from a regular expression to the given token code. No
   * provisions are taken to detect conflicting regular expressions, i.e.
   * regular expressions with common matches. To define a specific keyword,
   * e.g. <code>package</code> and also a general identifier, e.g.
   * <code>[a-z]+</code>, make sure to call <code>addToken</code> first
   * for the more specific token. Otherwise it will never be matched.</p>
   */
  public TrivialLexer<C> addToken(C tc, String regex) {
    Pattern p = Pattern.compile(regex);
    tokenInfos.add(new TokenInfo<C>(p, tc));
    return this;
  }
  /*+******************************************************************/
  @Override
  public C current() {
    return currentToken.getCode();
  }
  /*+******************************************************************/
  /**
   * <p>
   * discards the current token and advance to the next one. This may involve
   * skipping over input that cannot be matched by any regular expression
   * added with {@link #addToken}.
   * </p>
   * 
   * @return a token code or, on end of input, the specific token code
   *         provided to the constructor
   */
  @Override
  public C next() {
    countToken();
    while( restText.length()!=0 ) {
      for(TokenInfo<C> ti : tokenInfos) {
        Matcher m = ti.p.matcher(restText);
        //System.out.printf("lex check: %s%n", m.pattern().pattern());
        if( !m.lookingAt() ) continue;
        createToken(ti, m);
        return currentToken.getCode();
      }
      restText.deleteCharAt(0);
      column += 1;
    }
    currentToken = eofToken;
    return eofToken.getCode();
  }
  /* +***************************************************************** */
  /**
   * <p>returns the current token.</p>
   */
  public Token<C> currentToken() {
    return currentToken;
  }
  /* +***************************************************************** */
  private void countToken() {
    if( currentToken==null ) return;
    column += currentToken.getText().length();
  }
  /* +***************************************************************** */
  private void createToken(TokenInfo<C> ti, Matcher match) {
    String text = match.group();
    restText.delete(0, match.end());
    currentToken = new Token<C>(text, ti.c);
    //System.out.printf("%s: creating token %s%n", getClass().getName(),
    //                currentToken);
  }
  /*+******************************************************************/
  private static final class TokenInfo<C extends Enum<C>> {
    public final Pattern p;
    public final C c;
    public TokenInfo(Pattern p, C c) {
      this.p = p;
      this.c = c;
    }
  }
}
