package nodes;

import java.util.Arrays;
import java.util.List;

public class SimpleNode implements Node {
  private final Node[] children;
  public SimpleNode() {
    this.children = new Node[0];
  }
  public SimpleNode(List<Node> children) {
    if( children==null ) {
      throw new NullPointerException("children may not be null");
    }
    this.children = children.toArray(new Node[children.size()]);
  }
  public Iterable<Node> children() {
    return Arrays.asList(children);
  }

  public Node getChild(int i) {
    return (children)[i];
  }

  public int numChildren() {
    return children.length;
  }
}
