package absimpa;

import java.util.Formatter;
import java.util.Set;

/**
 * <p>
 * The parser may throw a {@code ParseException}, but it does
 * not create it itself. Rather it will call
 * {@link Lexer#parseException(Set)} to rely on the lexer to create it with
 * the necessary bits of information.
 * </p>
 * 
 * <p>
 * Strictly speaking this exception should be yet another generic parameter
 * to all the classes in this package, but that would make writing code that
 * uses them even more ugly.
 * </p>
 */
public class ParseException extends Exception {
  private static final int UNSET = -1;
  private final Set<?> expectedTokenCodes;
  private final Enum<?> foundTokenCode;
  private String moreInfo = null;
  
  private String tokenText = null;
  private String sourceName = null;
  private int line = UNSET;
  private int column = UNSET;
  /* +***************************************************************** */
  public ParseException(Set<?> expected, Enum<?> found) {
    this.expectedTokenCodes = expected;
    this.foundTokenCode = found;
  }
  /* +***************************************************************** */
  /**
   * creates a message from the fields set in this exception.
   */
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
    if( expectedTokenCodes.size()!=0 ) {
      if( expectedTokenCodes.size()!=1 ) {
        f.format(" but expected one of %s", expectedTokenCodes);
      } else {
        sb.append(" but expected `")
        .append(expectedTokenCodes.iterator().next())
        .append('\'');
      }
    }
    if( moreInfo!=null ) {
      sb.append("; ").append(moreInfo);
    }
                          
    return sb.toString();
  }
  /*+******************************************************************/
  public Set<?> getExpectedTokenCodes() {
    return expectedTokenCodes;
  }
  public Enum<?> getFoundTokenCode() {
    return foundTokenCode;
  }
  /*+******************************************************************/
  public int getColumn() {
    return column;
  }
  /**
   * <p>
   * set number of the input column within the input line on which
   * the parse error occurred.</p>
   */
  public void setColumn(int column) {
    if( column<0 ) throw new IllegalArgumentException("negative column");
    this.column = column;
  }
  public int getLine() {
    return line;
  }
  /**
   * <p>
   * set number of the input line (or anything resembling a line) on which
   * the parse error occurred.</p>
   */
  public void setLine(int line) {
    if( line<0 ) throw new IllegalArgumentException("negative line number");
    this.line = line;
  }
  public String getSourceName() {
    return sourceName;
  }
  /**
   * <p>
   * set the name of the source from which the line with the parse error was
   * read.
   * </p>
   */
  public void setSourceName(String sourceName) {
    this.sourceName = sourceName;
  }
  public String getTokenText() {
    return tokenText;
  }
  /**
   * <p>sets a string representation of the token that caused the error.</p>    
   */    
  public void setTokenText(String tokenText) {
    this.tokenText = tokenText;
  }
  /*+******************************************************************/
  public void setMoreInfo(String moreInfo) {
    this.moreInfo = moreInfo;
  }
  public String getMoreInfo() {
    return moreInfo;
  }
}
