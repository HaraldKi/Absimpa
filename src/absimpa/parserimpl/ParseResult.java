package absimpa.parserimpl;

import java.util.*;

class ParseResult<N> {
  private final List<N> nodes;
  private final boolean isEpsilon;
  private final boolean notApplicable;
  private static final ParseResult<?> instanceNotApplicable = 
    new ParseResult<>(false);
  private static final ParseResult<?> instanceIsEpsilon = 
    new ParseResult<>(true);
  /*+******************************************************************/
  public ParseResult(List<N> nodes) {
    this.isEpsilon = false;
    this.notApplicable = false;
    this.nodes = new ArrayList<>(nodes.size());
    this.nodes.addAll(nodes);
  }
  /*+******************************************************************/
  public ParseResult(N node) {
    this(node==null ? Collections.<N>emptyList() : Collections.singletonList(node));
  }
  /*+******************************************************************/
  private ParseResult(boolean isEpsilon) {
    if( isEpsilon ) {
      this.isEpsilon = true;
      this.notApplicable = false;
    } else {
      this.isEpsilon = false;
      this.notApplicable = true;
    }
    nodes = Collections.<N>emptyList();
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
  /* +***************************************************************** */
  public void addToNodeList(List<N> otherNodes) {
    otherNodes.addAll(this.nodes);
  }
  /* +***************************************************************** */
  public N getNode() {
    if( nodes.size()>1 ) {
      String msg =
          "parse result has more than one node, likely a forgotten "
              +"NodeFactory";
      throw new IllegalStateException(msg);
    }
    if( nodes.size()==0 ) {
      return null;
    }
    return nodes.get(0);
  }
  /*+******************************************************************/
  public boolean notApplicable() {
    return notApplicable;
  }
  public boolean isEpsilon() {
    return isEpsilon;
  }
}
