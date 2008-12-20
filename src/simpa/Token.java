/**
 * 
 */
package simpa;

import lexer.TokenCode;

public final class Token implements lexer.Token {
  public static enum Code implements TokenCode {
    TERM, PHRASE, SCOPE, OR, NOT, EOF;
  }
  /*+******************************************************************/
  public final Token.Code code;
  public final String text;
  /*+******************************************************************/
  public Token(Token.Code code, String text) {
    this.code = code;
    this.text = text;
  }
  /*+******************************************************************/
  public TokenCode getCode() {
    return code;
  }
  /*+******************************************************************/
  public String getText() {
    return text;
  }
  /*+******************************************************************/
  public String toString() {
    return String.format("%s[%s]", code, text);
  }
  /*+******************************************************************/
}