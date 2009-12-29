package example;


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import absimpa.*;

/**
 * <p>
 * is a trival implementation of a {@link absimpa.Lexer} which analyzes
 * a string by trying out regular expressions for tokens until a match is
 * found. This is not intended for productive use. It is merely an example.
 * </p>
 * 
 * <p>
 * This lexer is set up by specifying a list of pairs (regex, C), where
 * <code>C</code> is some enumeration type, the generic parameter of this
 * class. To analyze an input string, the lexer tries to match each of the
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
public class TrivialLexer<N,C extends Enum<C> & LeafFactory<N,C>> implements Lexer<N,C> {
  private final List<TokenInfo<N,C>> tokenInfos = new ArrayList<TokenInfo<N,C>>();
  private final Token<N,C> eofToken;

  private final StringBuilder restText = new StringBuilder();  
  private Token<N,C> currentToken = null;

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
    eofToken = new Token<N,C>("", eofCode);
  }
  /*+******************************************************************/
  /**
   * <p>
   * resets the lexer and initializes it to analyze the given
   * <code>text</code>. To prepare the first token, {@link #next} is
   * called internally.
   * </p>
   */
  public void initAnalysis(CharSequence text) {    
    restText.setLength(0);
    restText.append(text);
    line = 1;
    column = 1;
    currentToken = null;
    nextToken();
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
  public TrivialLexer<N,C> addToken(C tc, String regex) {
    Pattern p = Pattern.compile(regex);
    tokenInfos.add(new TokenInfo<N,C>(p, tc));
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
  public N next() {
    C code = currentToken.getCode();
    N node = code.create(this);
    nextToken();
    return node;
  }
  
  /*+******************************************************************/
  private void nextToken() {
    countToken();
    while( restText.length()!=0 ) {
      for(TokenInfo<N,C> ti : tokenInfos) {
        Matcher m = ti.p.matcher(restText);
        if( !m.lookingAt() ) continue;
        createCurrentToken(ti, m);
        return;
      }
      restText.deleteCharAt(0);
      column += 1;
    }
    currentToken = eofToken;
  }
  /* +***************************************************************** */
  /**
   * <p>returns the current token.</p>
   */
  public Token<N,C> currentToken() {
    return currentToken;
  }
  /* +***************************************************************** */
  public String currentText() {
    return currentToken.getText();
  }
  /* +***************************************************************** */
  private void countToken() {
    if( currentToken==null ) return;
    column += currentToken.getText().length();
  }
  /* +***************************************************************** */
  private void createCurrentToken(TokenInfo<N,C> ti, Matcher match) {
    String text = match.group();
    restText.delete(0, match.end());
    currentToken = new Token<N,C>(text, ti.c);
    //System.out.printf("%s: creating token %s%n", getClass().getName(),
    //                currentToken);
  }
  /*+******************************************************************/
  private static final class TokenInfo<N,C extends Enum<C>> {
    public final Pattern p;
    public final C c;
    public TokenInfo(Pattern p, C c) {
      this.p = p;
      this.c = c;
    }
  }
}
