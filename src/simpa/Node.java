package simpa;

public interface Node {
  public static enum Type {
    TERM, PHRASE, SCOPE, NEGATED, DISJUNCTION, CONJUNCTION;
  }
 
  Type getType();
  void addChild(Node node);

}
