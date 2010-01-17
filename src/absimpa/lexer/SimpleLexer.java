package absimpa.lexer;


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import example.LeafFactory;
import example.Token;

import absimpa.*;

/**
 * <p>
 * is a trival implementation of a {@link absimpa.Lexer} which analyzes a
 * string by trying out regular expressions for tokens until a match is
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
 * the input is converted to an {@code N} by means of the {@code LeafFactory}
 * implemented by the {@code C} type. The result is returned, while the lexer
 * starts over with the next token.
 * </p>
 * 
 * <p>
 * If no match can be found, the behaviour depends on whether
 * {@link #setSkipRe setSkipRe()} was called. If yes, the regular expression
 * is tried, and if it matches, the corresponding text is ignored and the
 * lexer starts over trying to match the regular expressions. If the skip
 * regular expression does not match, a ParserException is thrown. If no
 * regular expression to skip was set, or if it was set to {@code null}, the
 * lexer behaves as if every non-matching character may be skipped.
 * Consequently, input that cannot be matched is then silently discarded.
 * </p>
 * 
 * @param <C> is an enumeration and describes the token codes provided to the
 *        parser. In addition, the enum know how to transform a token code
 *        into an {@code N}
 * @param <N> is the date type returned for a token when the parser has
 *        recognized it and calles {@link #next}
 */
public class SimpleLexer<N,C extends Enum<C>> implements Lexer<N,C> {
  private final List<TokenInfo<N,C>> tokenInfos = new ArrayList<TokenInfo<N,C>>();
  private final Token<N,C> eofToken;

  private final LeafFactory<N,C> leafFactory;
  
  private final StringBuilder restText = new StringBuilder();  
  private Token<N,C> currentToken = null;

  private int line;
  private int column;
  
  private Pattern skip = null;
  /*+******************************************************************/
  /**
   * <p>
   * creates a <code>TrivialLexer</code> to return <code>eofCode</code>
   * when the end of input is encountered.
   * </p>
   */
  public SimpleLexer(C eofCode, LeafFactory<N,C> leafFactory) {
    eofToken = new Token<N,C>("", eofCode);
    this.leafFactory = leafFactory; 
  }
  /* +***************************************************************** */
  /**
   * <p>
   * adds all constants found in class {@tokenCode} with {@link #addToken}
   * except if it is identical to the {@link LexerInfo#eofCode} it provides.
   * It is assumed, that {@code toString()} of a code returns a regular
   * expression that defines the strings representing the token.
   * </p>
   * <p>
   * <b>IMPORTANT:</b>Make sure to define the code constants of {@code <C>}
   * in the order you want the regular expressions tried out by the lexer.
   */
  public <CC extends Enum<C> & LexerInfo<C>> SimpleLexer(
                                                         Class<CC> tokenCode,
                                                         LeafFactory<N,C> leafFactory) {
    CC[] codes = tokenCode.getEnumConstants();
    C eofCode = codes[0].eofCode();
    eofToken = new Token<N,C>("", eofCode);
    this.leafFactory = leafFactory;
    for(CC cc : codes) {
      if( cc==eofCode ) continue;
      @SuppressWarnings("unchecked")
      C c = (C)cc;
      addToken(c, cc.getRegex());
    }
  }
  /*+******************************************************************/
  /**
   * <p>
   * resets the lexer and initializes it to analyze the given
   * <code>text</code>. To prepare the first token, {@link #next} is
   * called internally.
   * </p>
   */
  public void initAnalysis(CharSequence text) throws ParseException {    
    restText.setLength(0);
    restText.append(text);
    line = 1;
    column = 1;
    currentToken = null;
    nextToken();
  }
  /*+******************************************************************/
  public void setSkipRe(String regex) {
    skip = Pattern.compile(regex);
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
  public SimpleLexer<N,C> addToken(C tc, String regex) {
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
   * @throws ParseException<C> 
   */
  @Override
  public N next() throws ParseException {
    N node = leafFactory.create(this);
    nextToken();
    return node;
  }
  
  /*+******************************************************************/
  private void nextToken() throws ParseException {
    countToken();
    while( restText.length()!=0 ) {
      for(TokenInfo<N,C> ti : tokenInfos) {
        Matcher m = ti.p.matcher(restText);
        if( !m.lookingAt() ) continue;
        createCurrentToken(ti, m);
        return;
      }
      if( skip!=null ) {
        applySkip();
      } else {
        restText.deleteCharAt(0);
        column += 1;
      }
    }
    currentToken = eofToken;
  }
  /*+******************************************************************/
  private void applySkip() throws ParseException {
    Matcher m = skip.matcher(restText);
    if( !m.lookingAt() ) {
      ParseException e = parseException(Collections.<C>emptySet());
      String snippet;
      int SNIPLEN = 20;
      if( restText.length()>SNIPLEN+3 ) {
        snippet = restText.substring(0, SNIPLEN)+"...";
      } else {
        snippet = restText.toString();
      }
      e.setMoreInfo("cannot then find valid token looking at: "+snippet);
      throw e;
    }
    column += m.end();
    restText.delete(0, m.end());    
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
  /*+******************************************************************/
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("TrivialLexer[(").append(currentToken.getCode())
    .append(",").append(currentText()).append(") \"");
    if( restText.length()>12 ) {
      sb.append(restText.substring(0, 9)).append("...");
    } else {
      sb.append(restText);
    }
    sb.append("\"]");
    return sb.toString();
  }
}
