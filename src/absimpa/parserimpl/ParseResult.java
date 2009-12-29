package absimpa.parserimpl;

class ParseResult<N> {
  private final N node;
  private final boolean isEpsilon;
  private final boolean notApplicable;
  private static final ParseResult<?> instanceNotApplicable = 
    new ParseResult<Object>(false);
  private static final ParseResult<?> instanceIsEpsilon = 
    new ParseResult<Object>(true);
  /*+******************************************************************/
  public ParseResult(N node) {
    this.isEpsilon = false;
    this.notApplicable = false;    
    this.node = node;
  }
  private ParseResult(boolean isEpsilon) {
    if( isEpsilon ) {
      this.isEpsilon = true;
      this.notApplicable = false;
    } else {
      this.isEpsilon = false;
      this.notApplicable = true;
    }
    node = null;
  }
  /*+******************************************************************/
  public static <T> ParseResult<T> ISEPSILON() {
    @SuppressWarnings("unchecked")
    ParseResult<T> r = (ParseResult<T>)instanceIsEpsilon;
    return r;
  }
  /*+******************************************************************/
  public static <T> ParseResult<T> NOTAPPLICABLE() {
    @SuppressWarnings("unchecked")
    ParseResult<T> r = (ParseResult<T>)instanceNotApplicable;
    return r;
  }
  /*+******************************************************************/
  N getNode() {
    return node;
  }
  public boolean notApplicable() {
    return notApplicable;
  }
  public boolean isEpsilon() {
    return isEpsilon;
  }
}
