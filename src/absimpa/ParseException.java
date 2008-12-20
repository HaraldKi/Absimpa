package absimpa;

import java.util.Formatter;
import java.util.Set;

public class ParseException extends Exception {
  private static final int UNSET = -1;
  private final Set expectedTokenCodes;
  private final Enum foundTokenCode;

  private String tokenText = null;
  private String sourceName = null;
  private int line = UNSET;
  private int column = UNSET;
  /* +***************************************************************** */
  public ParseException(Set expected, Enum found) {
    this.expectedTokenCodes = expected;
    this.foundTokenCode = found;
  }
  /* +***************************************************************** */
  @Override
  public String getMessage() {
    StringBuilder sb = new StringBuilder();
    Formatter f = new Formatter(sb);
    if( sourceName!=null ) f.format("%s:", sourceName);
    if( line!=UNSET ) {
      f.format("%d", line);
      if( column!=UNSET ) f.format(":%d", column);
      sb.append(':');
    }
    f.format("found token `%s", foundTokenCode);
    if( tokenText!=null ) f.format("(%s)'", tokenText);
    else sb.append('\'');
    f.format(" but expected one of %s", expectedTokenCodes);
    return sb.toString();
  }
  /*+******************************************************************/
  public Set getExpectedTokenCodes() {
    return expectedTokenCodes;
  }
  public Enum getFoundTokenCode() {
    return foundTokenCode;
  }
  /*+******************************************************************/
  public int getColumn() {
    return column;
  }
  public void setColumn(int column) {
    if( column<0 ) throw new IllegalArgumentException("negative column");
    this.column = column;
  }
  public int getLine() {
    return line;
  }
  public void setLine(int line) {
    if( line<0 ) throw new IllegalArgumentException("negative line number");
    this.line = line;
  }
  public String getSourceName() {
    return sourceName;
  }
  public void setSourceName(String sourceName) {
    this.sourceName = sourceName;
  }
  public String getTokenText() {
    return tokenText;
  }
  public void setTokenText(String tokenText) {
    this.tokenText = tokenText;
  }
}
