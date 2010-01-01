package absimpa.wisent;

import java.util.*;

import example.LeafFactory;
import example.TrivialLexer;

import absimpa.*;

public class BNF<N, C extends Enum<C>> {
  // For grammars entered with rule();
  private final Map<String,Grammar<N,C>> syntaxMap;
  
  // For grammar names not yet entered with rule()
  private final Map<String,Recurse<N,C>> placeHolders =
    new HashMap<String,Recurse<N,C>>();

  // our lexer and parser
  private final TrivialLexer<Node,TokenCode> lex;
  private final Parser<Node,TokenCode> parser;
  
  // only used during parsing to allow MyLeafs to fetch the next node factory
  private List<NodeFactory<N>> nfmarkStack = null;
  
  /*+******************************************************************/
  private static enum TokenCode  {
    EOF, SYMBOL, STAR, PLUS, OPAREN, CPAREN, QUESTIONMARK, BAR, PERCENT;
  }
  /* +***************************************************************** */
  public BNF(Class<C> enumClass) {
    // This is what we want to parse as right side of rules
    // expr -> product ( "|" product)*
    // product -> term (term)*
    // term -> literal (oper)?
    // oper -> "+" | "*" | "?"
    // literal -> symbol | "%"? "(" expr ")"
    // symbol -> {any token registered by the user}

    C[] constants = enumClass.getEnumConstants();
    syntaxMap = new HashMap<String,Grammar<N,C>>(constants.length);
    for(C c : constants) {
      syntaxMap.put(c.toString(), new TokenGrammar<N,C>(c));
    }
    absimpa.GrammarBuilder<Node,TokenCode> gb =
        new absimpa.GrammarBuilder<Node,TokenCode>(null);

    Recurse<Node,TokenCode> recurse = new Recurse<Node,TokenCode>();

    Grammar<Node,TokenCode> symbol = gb.token(TokenCode.SYMBOL);
    Grammar<Node,TokenCode> oparen = gb.token(TokenCode.OPAREN);
    Grammar<Node,TokenCode> cparen = gb.token(TokenCode.CPAREN);
    Grammar<Node,TokenCode> star = gb.token(TokenCode.STAR);
    Grammar<Node,TokenCode> plus = gb.token(TokenCode.PLUS);
    Grammar<Node,TokenCode> questionmark = gb.token(TokenCode.QUESTIONMARK);
    Grammar<Node,TokenCode> bar = gb.token(TokenCode.BAR);
    Grammar<Node,TokenCode> nfmark = gb.token(TokenCode.PERCENT);

    Grammar<Node,TokenCode> parenExpr =
        gb.seq(gb.opt(nfmark), oparen, recurse, cparen)
          .setNodeFactory(makeNodeFactory);

    Grammar<Node,TokenCode> literal = gb.choice(symbol, parenExpr);

    Grammar<Node,TokenCode> oper = gb.choice(plus, star, questionmark);

    Grammar<Node,TokenCode> term =
        gb.seq(literal, gb.opt(oper)).setNodeFactory(makeLiteral);

    Grammar<Node,TokenCode> product =
        gb.repeat(term, 1, Integer.MAX_VALUE).setNodeFactory(makeProduct);

    Grammar<Node,TokenCode> expr =
        gb.seq(product, gb.seq(bar, product).star()).setNodeFactory(makeOr);

    recurse.setChild(expr);

    parser = expr.compile();

    lex = new TrivialLexer<Node,TokenCode>(TokenCode.EOF, new MyLeafs());
    lex.addToken(TokenCode.SYMBOL, "[A-Za-z][A-Za-z0-9]*").addToken(TokenCode.STAR,
                                                            "[*]")
      .addToken(TokenCode.PLUS, "[+]").addToken(TokenCode.OPAREN, "[(]")
      .addToken(TokenCode.CPAREN, "[)]").addToken(TokenCode.QUESTIONMARK,
                                                  "[?]")
      .addToken(TokenCode.BAR, "[|]").addToken(TokenCode.PERCENT, "[%]");
    lex.setSkipRe("[\\s]+");
  }
  /*+******************************************************************/
  private Grammar<N,C> placeHolderGrammar(String symbolName) {
    Grammar<N,C> placeHolder = placeHolders.get(symbolName);
    if( placeHolder!=null ) return placeHolder;
    
    Recurse<N,C> g = new Recurse<N,C>();
    placeHolders.put(symbolName, g);
    return g;
  }
  /*+******************************************************************/
  private final NodeFactory<Node> makeNodeFactory = new NodeFactory<Node>() {
    @Override
    public Node create(List<Node> children) {
      if( children.size()==1 ) {
        return children.get(0);
      }
      assert children.size()!=2 : "must have one or two children but have "+children.size(); 
      assert children.get(0).code == TokenCode.PERCENT;
      
      Node childNode = children.get(1);
      Grammar<N,C> child = childNode.getGrammar();
      NodeFactory<N> nf = children.get(0).getNodeFactory();
      child.setNodeFactory(nf);
      return childNode;
    }  
  };
  /*+******************************************************************/
  private final NodeFactory<Node> makeLiteral= new NodeFactory<Node>() {
    @Override
    public Node create(List<Node> children) {
      if( children.size()==1 ) {
        return children.get(0);
      }
      assert children.size()!=2 : "must have one or two children but have "+children.size(); 

      Grammar<N,C> child = children.get(0).getGrammar();
      switch( children.get(1).getCode() ) {
      case STAR:
        return new Node(new Repeat<N,C>(0, Integer.MAX_VALUE, child));
      case QUESTIONMARK:
        return new Node(new Repeat<N,C>(0, 1, child));
      case PLUS:
        return new Node(new Repeat<N,C>(1, Integer.MAX_VALUE, child));
      default:
        assert false : "should not have TokenCode "+children.get(1).getCode();
      }
      return null;
    }
  };
  /*+******************************************************************/
  private final NodeFactory<Node> makeProduct = new NodeFactory<Node>() {
    @Override
    public Node create(List<Node> children) {
      if( children.size()==1 ) {
        return children.get(0);
      }
      Sequence<N,C> result = new Sequence<N,C>(children.get(0).getGrammar());
      for(int i=1; i<children.size(); i++) {
        result.add(children.get(i).getGrammar());
      }
      return new Node(result);
    }    
  };
  /*+******************************************************************/
  private final NodeFactory<Node> makeOr = new NodeFactory<Node>() {
    @Override
    public Node create(List<Node> children) {
      if( children.size()==1 ) {
        return children.get(0);
      }
      Choice<N,C> result = new Choice<N,C>(children.get(0).getGrammar());
      for(int i=1; i<children.size(); i++) {
        result.or(children.get(i).getGrammar());
      }
      return new Node(result);
    }
  };
  /*+******************************************************************/
  public Grammar<N,C> getGrammar(String name) {
    return syntaxMap.get(name);
  }
  /* +***************************************************************** */
  public Grammar<N,C> rule(String name, String expansion) throws ParseException {
    return rule(name, expansion, Collections.<NodeFactory<N>>emptyList()); 
  }
  /* +***************************************************************** */
  public Grammar<N,C> rule(String name, String expansion, NodeFactory<N> nf) throws ParseException {
    List<NodeFactory<N>> l = new ArrayList<NodeFactory<N>>(1);
    l.add(nf);
    return rule(name, expansion, l);
  }
  /* +***************************************************************** */
  public Grammar<N,C> rule(String name, String expansion, 
                   NodeFactory<N> nf1, NodeFactory<N> nf2) throws ParseException {
    List<NodeFactory<N>> l = new ArrayList<NodeFactory<N>>(2);
    l.add(nf1);
    l.add(nf2);
    return rule(name, expansion, l);
  }
  /*+******************************************************************/
  public Grammar<N,C> rule(String name, String expansion, 
                   NodeFactory<N> nf1, 
                   NodeFactory<N> nf2,
                   NodeFactory<N> ...more) throws ParseException {
    List<NodeFactory<N>> l = new ArrayList<NodeFactory<N>>(2+more.length);
    l.add(nf1);
    l.add(nf2);
    for(NodeFactory<N> nf : more) l.add(nf);
    return rule(name, expansion, l);
  }
  /* +***************************************************************** */
  private Grammar<N,C> rule(String name, String expansion,
                   List<NodeFactory<N>> nodeFactories) throws ParseException
  {
    if( syntaxMap.containsKey(name) ) {
      throw new IllegalArgumentException(name+" already defined.");
    }
    nfmarkStack = nodeFactories;
    
    lex.initAnalysis(expansion);
    Node node = parser.parse(lex);

    Recurse<N,C> placeHolder = placeHolders.remove(name);
    if( placeHolder!=null ) {
      Grammar<N,C> child = node.getGrammar();
      child.setName(name);
      placeHolder.setChild(child);
      //placeHolder.setName(name);
      syntaxMap.put(name, child);
      return child;
    } else {
      Grammar<N,C> g = node.getGrammar();
      g.setName(name);
      syntaxMap.put(name, g);
      return g;
    }
  }
  /*+******************************************************************/
  public Parser<N,C> compile(String ruleNname) {
    Grammar<N,C> g = syntaxMap.get(ruleNname);
    if( placeHolders.size()>0 ) {
      StringBuilder sb = new StringBuilder();
      sb.append("the following names are undefined:");
      for(String name : placeHolders.keySet()) {
        sb.append(' ').append(name);
      }
      throw new IllegalStateException(sb.toString());
    }
    return  g.compile();
  }
  /*+******************************************************************/
  private class MyLeafs implements LeafFactory<Node,TokenCode> {
    @Override
    public Node create(TrivialLexer<Node,TokenCode> lex) throws ParseException {
      TokenCode current = lex.current();
      
      switch( current ) {
      case SYMBOL:
        String symbolName = lex.currentText();
        Grammar<N,C> g = syntaxMap.get(symbolName);
        if( g==null ) {
          g = placeHolderGrammar(symbolName);
        }
        return new Node(g);        
      case STAR:
      case PLUS:
      case QUESTIONMARK:
        return new Node(current);
      case PERCENT:
        if( nfmarkStack.size()<1 ) {
          ParseException e = lex.parseException(Collections.<TokenCode>emptySet());
          e.setMoreInfo("not enough node factories");
          throw e;
        }
        return new Node(nfmarkStack.remove(0));
      default:
        return null;
      }
    }    
  }
  /*+******************************************************************/
  private class Node {
    private final Grammar<N,C> grammar;
    private final TokenCode code;
    private final NodeFactory<N> nodeFactory;
    public Node(Grammar<N,C> grammar) {
      this.grammar = grammar;
      this.code = null;
      this.nodeFactory = null;
    }
    public Node(TokenCode code) {
      this.grammar = null;
      this.code = code;
      this.nodeFactory = null;
    }
    public Node(NodeFactory<N> nodeFactory) {
      this.grammar = null;
      this.code = null;
      this.nodeFactory = nodeFactory;
    }
    public Grammar<N,C> getGrammar() {
      return grammar;
    }
    public TokenCode getCode() {
      return code;
    }
    public NodeFactory<N> getNodeFactory() {
      return nodeFactory;
    }
  }
  /*+******************************************************************/
}

