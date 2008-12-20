package simpa;

import simpa.Token.Code;
import static simpa.NodeImpl.*;

public class Parser {
  private final Lexer lex;
  private Token currentToken;
  
  public Parser(Lexer lex) {
    this.lex = lex;
  }
  public Node parse() {
    next();
    Node[] dis = disjunction();
    if( dis==null ) return null;
    
    Node con = conjunctionNode();
    for(Node n : dis) con.addChild(n);
    while( null!=(dis=disjunction()) ) {
      for(Node n : dis) con.addChild(n);
    }
    return con;
  }
  private void next() {
    currentToken = lex.next();
    //System.out.printf("--- %s%n", currentToken);
  }
  public Node[] disjunction() {
    Node elem = element();
    if( elem==null ) return null;

    if( elem.getType()==Type.NEGATED ) return nodeArray(elem);

    if( currentToken.code!=Code.OR ) return nodeArray(elem);
    
    Node dis = disjunctionNode(elem);
    while( currentToken.code==Code.OR ) {
      next();
      elem = element();
      if( elem==null ) break;
      
      if( elem.getType()==Type.NEGATED) {
        return nodeArray(dis, elem);
      } 
      dis.addChild(elem);
    }
    return nodeArray(dis);
  }

  public Node element() {
    boolean negated = false;
    if( currentToken.code==Code.NOT ) {
      negated = true;
      next();
    }
    Node scope = scoped();
    if( scope==null ) return null;
    if( negated ) return negatedNode(scope);
    else return scope;
  }
  
  public Node scoped() {
    String scopeName = null;
    if( currentToken.code==Code.SCOPE ) {
      scopeName = currentToken.text;
      next();
    }
    Node literal = literal();
    if( literal==null ) return null;
    if( scopeName!=null ) return scopeNode(scopeName, literal);
    else return literal;
  }
  public Node literal() {
    Node result = null;
    while( result==null ) {
      if( currentToken.code==Code.TERM) {
        result = termNode(currentToken.text);
        next();
      } else if( currentToken.code==Code.PHRASE ) {
        result = phraseNode(currentToken.text);
        next();
      } else if( currentToken.code==Code.EOF ) {
        return null;
      } else {
        System.out.printf("dropping %s%n", currentToken);
        next();
      }
    }
    return result;
  }
  /*+******************************************************************/
  public static void main(String[] argv) throws Exception {
    StringBuilder sb = new StringBuilder();
    for(String a : argv) {
      sb.append(a).append(' ');
    }
    Lexer lex = new Lexer(sb.toString());
    NodeImpl top =(NodeImpl)(new Parser(lex).parse());
    top.dump(System.out, "");
  }
}
