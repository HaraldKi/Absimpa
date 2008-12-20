package nodes;

public interface Node {
  Iterable<Node> children();
  int numChildren();
  Node getChild(int i);
}
