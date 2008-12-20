package simpa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NodeImpl implements Node {
  private static enum Arity {
    NULL, ONE, MANY;
  }
  private final Type type;
  private final Arity arity;
  private final List<Node> children = new ArrayList<Node>(1);
  
  public NodeImpl(Type type, Arity arity) {
    this.type = type;
    this.arity = arity;
  }
  public Type getType() {
    return type;
  }
  public void addChild(Node child) {
    if( arity==Arity.NULL ) throw new IllegalStateException();
    if( arity==Arity.ONE && children.size()>0 ) {
      throw new IllegalStateException();
    }
    children.add(child);
  }
  public static Node termNode(String term) {
    return new TermNode(term);
  }
  public static Node phraseNode(String term) {
    return new PhraseNode(term);
  }
  public static Node scopeNode(String scope, Node child) {
    Node n = new ScopeNode(scope);
    n.addChild(child);
    return n;
  }
  public static Node negatedNode(Node child) {
    Node n = new NodeImpl(Type.NEGATED, Arity.ONE);
    n.addChild(child);
    return n;
  }
  public static Node disjunctionNode(Node firstChild) {
    Node n = new NodeImpl(Type.DISJUNCTION, Arity.MANY);
    n.addChild(firstChild);
    return n;
  }
  public static Node disjunctionNode() {
    Node n = new NodeImpl(Type.DISJUNCTION, Arity.MANY);
    return n;
  }
  public static Node conjunctionNode() {
    Node n = new NodeImpl(Type.CONJUNCTION, Arity.MANY);
    return n;
  }
   public static Node[] nodeArray(Node... nodes) {
    return nodes;
  }
  /*+******************************************************************/
  private static class TermNode extends NodeImpl {
    private final String term;
    public TermNode(String term) {
      super(Type.TERM, Arity.NULL);
      this.term = term;
    }
    public String toString() {
      return String.format("LEAF[%s]", term);
    }
  }
  /*+******************************************************************/
  private static class PhraseNode extends NodeImpl {
    private final String phrase;
    public PhraseNode(String term) {
      super(Type.PHRASE, Arity.NULL);
      this.phrase = term;
    }
    public String toString() {
      return String.format("PHRASE[%s]", phrase);
    }
  }
  /*+******************************************************************/
  private static class ScopeNode extends NodeImpl {
    private final String scope;
    public ScopeNode(String scope) {
      super(Type.SCOPE, Arity.ONE);
      this.scope = scope;
    }
    public String toString() {
      return String.format("SCOPE[%s]", scope);
    }
  }
  /*+******************************************************************/
  public void dump(Appendable app, String indent) throws IOException {
    app.append(indent).append(toString()).append("\n");
    for(Node child : children) {
      NodeImpl d = (NodeImpl)child;
      d.dump(app, "  "+indent);
    }
  }
  /*+******************************************************************/
  public String toString() {
    return type.toString();
  }
  /*+******************************************************************/
}
