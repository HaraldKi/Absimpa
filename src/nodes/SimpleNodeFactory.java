package nodes;


import java.util.List;

import absimpa.*;

import lexer.Token;


public class SimpleNodeFactory<C extends Enum<C>> implements NodeFactory<Node,C>
{
  public ValueNode<Token<C>> create(Token<C> token) {
    return new ValueNode<Token<C>>(token);
  }

  public Node create(List<Node> children) {
    return new SimpleNode(children);
  }

}
