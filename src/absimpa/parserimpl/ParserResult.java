package absimpa.parserimpl;

public class ParserResult<N> {
  private final N node;
  private final boolean notApplicable; 
  private final boolean epsilon;
  private final boolean isNode;
  
  private static final ParserResult<?> EPSILON = 
    new ParserResult<Object>(false, true);
  private static final ParserResult<Object> NOTAPPLICABLE = 
    new ParserResult<Object>(true, false);
    

  public static <E> ParserResult<E> EPSILON() {
    @SuppressWarnings("unchecked")
    ParserResult<E> result = (ParserResult<E>)EPSILON;
    return result;
  }
  public static <E> ParserResult<E> NOTAPPLICABLE() {
    @SuppressWarnings("unchecked")
    ParserResult<E> result = (ParserResult<E>)NOTAPPLICABLE;
    return result;
  }
  private ParserResult(boolean notApplicable, boolean epsilon) {
    this.node = null;
    this.isNode = false;
    this.notApplicable = notApplicable;
    this.epsilon = epsilon;
  }
  public ParserResult(N node) {
    this.node = node;
    this.isNode = true;
    this.epsilon = false;
    this.notApplicable = false;
  }
  
  public N get() {
    if( isNode ) return node;
    throw new IllegalStateException("result is not a result with a node");
  }
  
  public boolean notApplicable() {
    return notApplicable;
  }
  
  public boolean isEpsilon() {
    return epsilon;
  }
  
  public boolean isNode() {
    return isNode;
  }
}
