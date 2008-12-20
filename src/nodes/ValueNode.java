package nodes;

import java.util.List;

/**
 * 
 * @param <V> is the type of the value stored in the node.
 */
public class ValueNode<V> extends SimpleNode {

  private V value;

  /*+******************************************************************/
  public ValueNode(V value) {
    super();
    this.value = value;
  }  
  public ValueNode(V value, List<Node> children) {
    super(children);
    this.value = value;
  }  
  public V getValue() {
    return value;
  }
  /*+******************************************************************/
}
