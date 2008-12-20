package parser;

import grammar.ParseException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lexer.Lexer;

public class MostTrivialLexer<C extends Enum<C>> implements Lexer<C> {
  private final List<TokenInfo> tokenInfos = new ArrayList<TokenInfo>();
  private final TestToken<C> eofToken;
  private final StringBuilder restText = new StringBuilder();
  
  private TestToken<C> currentToken = null;

  private int line;
  private int column;
  /*+******************************************************************/
  public MostTrivialLexer(C eofCode) {
    eofToken = new TestToken<C>("", eofCode);
  }
  /*+******************************************************************/
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
  public MostTrivialLexer<C> addToken(C tc, String regex) {
    Pattern p = Pattern.compile(regex);
    tokenInfos.add(new TokenInfo<C>(p, tc));
    return this;
  }
  @Override
  public C current() {
    return currentToken.getCode();
  }

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
  public TestToken<C> currentToken() {
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
    currentToken = new TestToken<C>(text, ti.c);
    //System.out.printf("%s: creating token %s%n", getClass().getName(),
    //                currentToken);
  }
  /*+******************************************************************/
  public static final class TestToken<C extends Enum<C>> {
    private final String text;
    private final C c;
    public TestToken(String text, C c) {
      this.text = text;
      this.c = c;
    }
    public C getCode() {
     return c;
    }
    public String getText() {
      return text;
    }
    public String toString() {
      return String.format("%s(\"%s\")", c, text);
    }
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
