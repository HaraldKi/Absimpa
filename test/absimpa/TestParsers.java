package absimpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.*;

import nodes.Node;
import nodes.ValueNode;

import org.junit.Before;
import org.junit.Test;

import absimpa.bnf.SimpleLexer;

import example.*;



public class TestParsers {
  private static class L extends SimpleLexer<TestNode,Codes> {
    public L(Codes eofCode) {
      super(eofCode, leafFactory);
    }    
    @Override
    public L addToken(Codes tc, String regex) {
      super.addToken(tc, regex);
      return this;
    }
  }

  private L lex;
  private Grammar<TestNode,Codes> term;
  private Grammar<TestNode,Codes> and;
  private Grammar<TestNode,Codes> or;
  private Grammar<TestNode,Codes> not;
  private Grammar<TestNode,Codes> scopename;

  private final GrammarBuilder<TestNode,Codes> gb = 
    new GrammarBuilder<TestNode,Codes>(NodeType.DEFAULT);

  private static enum Codes implements LeafFactory<TestNode,Codes> {
    SCOPE, TERM, AND, OR, POPEN, PCLOSE, NOT, EOF;
    @Override
    public TestNode create(SimpleLexer<TestNode,Codes> lex) {
      return new LeafNode(lex.currentToken());
    }
  }
  /*+******************************************************************/
  private static LeafFactory<TestNode,Codes> leafFactory = new LeafFactory<TestNode,Codes>() {
    @Override
    public TestNode create(SimpleLexer<TestNode,Codes> lex)
      throws ParseException
    {
      return lex.current().create(lex);
    }
    
  };
  /* +***************************************************************** */
  private static enum NodeType implements NodeFactory<TestNode>{
    DEFAULT, TOKEN, AND, OR, ORS, ORP, SCOPE, TERMLIST, SEQ, NOT, OPENPAREN, CLOSEPAREN;

    public TestNode create(List<TestNode> children) {
      if( children.size()==0 ) return null;
      List<TestNode> flattened = flattenDefaultNodes(children);
      return new TestNode(this, flattened);
    }
    private static List<TestNode> flattenDefaultNodes(List<TestNode> l) {
      List<TestNode> result = new ArrayList<TestNode>(l.size());
      for(TestNode n : l) {
        if( DEFAULT==n.getValue() ) {
          for(Node child : n.children())
            result.add((TestNode)child);
        } else {
          result.add(n);
        }
      }
      return result;
    }
  }
  /*+******************************************************************/
  // shortcut to get rid of long generic params
  //private static interface MyParser extends Parser<TestNode,Codes> {}
  /*+******************************************************************/
  private static class TestNode extends ValueNode<NodeType> {
    public TestNode(NodeType t, List<TestNode> children) {
      super(t, new ArrayList<Node>(children));
    }
    public NodeType getChildType(int i) {
      TestNode child = (TestNode)getChild(i);
      return child.getValue();
    }
    public void dump(Appendable app) throws IOException {
      dump(app, "");
    }
    protected void dump(Appendable app, String indent ) throws IOException {
      app.append(indent).append(getValue().toString()).append("\n");
      dumpChildren(app, indent);
    }
    protected void dumpChildren(Appendable app, String indent)
      throws IOException
    {
      indent = "  "+indent;
      for(Node child : super.children()) {
        if( child==null ) {
          app.append(indent).append("ERROR: null child\n");
        } else {
          ((TestNode)child).dump(app, indent);
        }
      }
    }
    public String toString() {
      StringBuilder sb = new StringBuilder();
      try {dump(sb);} catch(IOException e) {throw new Error("gnarg");}
      return sb.toString();
    }
  }
  /*+******************************************************************/
  private static class LeafNode extends TestNode {
    private final Token<TestNode, Codes> t;
    @SuppressWarnings("unchecked")
    public LeafNode(Token t) {
      super(NodeType.TOKEN, Collections.EMPTY_LIST);
      this.t = t;
    }
    public Token<TestNode, Codes> getToken() {
      return t;
    }
    protected void dump(Appendable app, String indent) throws IOException {
      app.append(indent).append(getValue().toString())
      .append('[').append(t.toString()).append("]\n");
      dumpChildren(app, indent);
    }
  }
  /*+******************************************************************/
  @Before
  public void setup() {
    lex = new L(Codes.EOF)
    .addToken(Codes.SCOPE, "[A-Za-z]+:")
    .addToken(Codes.AND, "AND")
    .addToken(Codes.OR, "OR")
    .addToken(Codes.NOT, "[-]")
    .addToken(Codes.NOT, "NOT")
    .addToken(Codes.TERM, "[A-Za-z]+")
    .addToken(Codes.POPEN, "[(]")
    .addToken(Codes.PCLOSE, "[)]")
    ;
    term = gb.token(Codes.TERM);
    and = gb.token(Codes.AND);
    or = gb.token(Codes.OR);
    not = gb.token(Codes.NOT);
    scopename = gb.token(Codes.SCOPE);

  }
  /* +***************************************************************** */
  /* +***************************************************************** */
  @Test
  public void testTokenParser() throws Exception
  {
    Parser<TestNode,Codes> tp =
        gb.token(Codes.TERM).compile();
    lex.initAnalysis("abc ddd: ");
    checkTokenParse(tp, "abc", Codes.TERM);
  }
  private void checkTokenParse(Parser<TestNode,Codes> tp, String text,
                               Codes code) throws Exception
  {
    TestNode node = tp.parse(lex);
    //node.dump(System.out);
    assertTrue(node instanceof LeafNode);
    LeafNode n = (LeafNode)node;
    Token<TestNode,Codes> t = n.getToken();
    assertEquals(text, t.getText());
    assertEquals(code, t.getCode());
  }
  /* +***************************************************************** */
  @Test
  public void testSequenceParser() throws Exception {
    Parser<TestNode,Codes> scope = 
      gb.seq(NodeType.SCOPE, scopename).add(term).compile();

    lex.initAnalysis("name: value");
    checkSequenceParse(scope, NodeType.SCOPE, NodeType.TOKEN, NodeType.TOKEN);
  }
  private void checkSequenceParse(Parser<TestNode,Codes> scope,
                                  NodeType parent, NodeType child1,
                                  NodeType child2) throws Exception
  {
    TestNode node = scope.parse(lex);
    // node.dump(System.out);
    assertEquals(parent, node.getValue());
    assertEquals(child1, node.getChildType(0));
    assertEquals(child2, node.getChildType(1));
  }
  /* +***************************************************************** */
  private TestNode analyze(String text, Parser<TestNode,Codes> p)
    throws IOException, ParseException
  {
    lex.initAnalysis(text);
    TestNode node = p.parse(lex);
    //node.dump(System.out);
    return node;
  }
  /*+******************************************************************/
  @Test
  public void testRepeat2_2() throws Exception {
    Parser<TestNode,Codes> rep = makeRepeatGrammar(term, 2,2).compile();
    
    TestNode node = analyze("eins zwei drei vier", rep);
    assertEquals(NodeType.TERMLIST, node.getValue());
    assertEquals(NodeType.TOKEN, node.getChildType(0));
    assertEquals(NodeType.TOKEN, node.getChildType(1));
    assertEquals(2, node.numChildren());
  }
  @Test
  public void testRepeat0With0() throws Exception {
    Grammar<TestNode,Codes> rep = makeRepeatGrammar(scopename, 0,2);

    Grammar<TestNode,Codes> seq = gb.seq(NodeType.SEQ, rep).add(term);

    TestNode node = analyze("abc: xyz", seq.compile());

    assertEquals(NodeType.TERMLIST, node.getChildType(0));
    assertEquals(NodeType.TOKEN, node.getChildType(1));
    assertEquals(2, node.numChildren());
  }
  @Test
  public void testRepeatStopNonmatchingToken() throws Exception {
    Grammar<TestNode,Codes> rep = makeRepeatGrammar(term, 0, 5);

    TestNode node = analyze("eins zwei ende:", rep.compile());

    assertEquals(NodeType.TOKEN, node.getChildType(0));
    assertEquals(NodeType.TOKEN, node.getChildType(1));
    assertEquals(2, node.numChildren());

  }
  @Test
  public void testRepeatStopAtMax() throws Exception {
    Grammar<TestNode,Codes> rep = makeRepeatGrammar(term, 0, 2);

    TestNode node = analyze("eins zwei drei vier", rep.compile());

    assertEquals(NodeType.TOKEN, node.getChildType(0));
    assertEquals(NodeType.TOKEN, node.getChildType(1));
    assertEquals(2, node.numChildren());
  }
  @Test(expected=ParseException.class)
  public void testRepeatNotEnough() throws Exception {
    Grammar<TestNode,Codes> rep = makeRepeatGrammar(term, 4, 4);

    TestNode node = analyze("eins zwei ", rep.compile());

    assertEquals(NodeType.TOKEN, node.getChildType(0));
    assertEquals(NodeType.TOKEN, node.getChildType(1));
    assertEquals(2, node.numChildren());
  }
  private Grammar<TestNode,Codes> makeRepeatGrammar(Grammar<TestNode,Codes> g,
                                                     int min, int max)
  {
    Grammar<TestNode,Codes> rep = gb.repeat(NodeType.TERMLIST, g, min, max);
    return rep;
  }
  /*+******************************************************************/
  @Test
  public void testChoice() throws Exception {
    Parser<TestNode,Codes> choice = makeChoiceParser().compile();
    TestNode node = analyze("abc", choice);
    assertEquals(NodeType.TOKEN, node.getValue());
    assertEquals(Codes.TERM, getTokenCode(node));
    
    node = analyze(" OR", choice);
    assertEquals(NodeType.TOKEN, node.getValue());
    assertEquals(Codes.OR, getTokenCode(node));

    node = analyze("AND ", choice);
    assertEquals(NodeType.TOKEN, node.getValue());
    assertEquals(Codes.AND, getTokenCode(node));
  }
  @Test(expected=ParseException.class)
  public void testChoiceException() throws Exception {
    Parser<TestNode,Codes> choice = makeChoiceParser().compile();
    analyze("", choice);
  }
  private Codes getTokenCode(TestNode node) {
    LeafNode ln = (LeafNode)node;
    return ln.getToken().getCode();
  }
  private Choice<TestNode,Codes> makeChoiceParser() {
    Choice<TestNode,Codes> choice =
      gb.choice(term)
      .or(and)
      .or(or);
    return choice;
  }
  /*+******************************************************************/
  @Test
  public void testWithOptionalChoice() throws Exception {
    Grammar<TestNode,Codes> repeatOptional = makeRepeatGrammar(term, 0, 1);

    Choice<TestNode,Codes> choice =
      gb.choice(and).or(repeatOptional).or(or);

    TestNode node = analyze("ascope:", choice.compile());
    
    assertEquals(null, node);
  }
  /*+******************************************************************/
  @Test
  public void sequenceInChoice() throws Exception {

    Sequence<TestNode,Codes> seq = 
      gb.seq(NodeType.SCOPE, scopename).add(term);

    Choice<TestNode,Codes> choice = gb.choice(or).or(seq);

    TestNode node = analyze("abc: bla", choice.compile());
    
    assertEquals(NodeType.SCOPE, node.getValue());
    assertEquals(2, node.numChildren());
  }
  /* +***************************************************************** */
  @Test 
  public void notEnoughRepeat() throws Exception {
    Grammar<TestNode,Codes> rep =
      gb.repeat(NodeType.AND, term, 4, 1000);
    EnumSet<Codes> expected = codeSet(Codes.TERM);
    checkParseException("abc abc", rep.compile(), expected, Codes.EOF);
  }
  /* +***************************************************************** */
  @Test
  public void optionalInChoice() throws Exception {
    // optTerm -> term?
    Grammar<TestNode,Codes> optTerm =
      gb.repeat(NodeType.AND, term, 0, 1);

    // critChoice -> optTerm | and
    Grammar<TestNode,Codes> critChoice = 
      gb.choice(optTerm).or(and);

    // absimpa -> critChoice or
    Grammar<TestNode,Codes> grammar = 
      gb.seq(NodeType.SEQ, critChoice).add(or);

    TestNode node = analyze("OR", grammar.compile());
    //node.dump(System.out);
    assertEquals(NodeType.SEQ, node.getValue());
    assertEquals(NodeType.TOKEN, node.getChildType(0));
  }
  /* +***************************************************************** */
  @Test
  public void conflictSequenceLA() throws Exception
  {
    Grammar<TestNode,Codes> optTerm = gb.repeat(term, 0, 1);

    Parser<TestNode,Codes> p = 
      gb.seq(optTerm, term).setNodeFactory(NodeType.AND).compile();

    Exception e = null;
    try {
      // first come first server strategy of the sequence parser must result in
      // a parse error, because the first term, despite being optional, is
      // recognized. Then there is nothing left for the 2nd term to match.
      analyze("abc", p);
    } catch( ParseException ee ) {
      e = ee;
    }
    
    assertTrue(e!=null);
    //System.out.printf("%s%n", e.getMessage());
    assertTrue(e.getMessage().startsWith("1:4:found token `EOF()'"));
  }
  /* +***************************************************************** */
  private Parser<TestNode,Codes> miniLanguage() {
    return miniLanguageGrammar().compile();
  }
  /* +***************************************************************** */
  private Grammar<TestNode,Codes> miniLanguageGrammar()  {
    // scope -> scopename term
    Grammar<TestNode,Codes> scope = 
      gb.seq(scopename, term).setNodeFactory(NodeType.SCOPE);
    // literal -> scope | literal
    Grammar<TestNode,Codes> literal =  gb.choice(term).or(scope);

    // orlist -> literal (or literal)*
    Grammar<TestNode,Codes> orlist =
        gb.seq(literal, gb.star(gb.seq(or,literal))).setNodeFactory(NodeType.OR);
    
    // negated -> not literal
    Grammar<TestNode,Codes> negated =
      gb.seq(not, literal).setNodeFactory(NodeType.NOT);

    // grammar -> (orlist | negated)+
    Grammar<TestNode,Codes> grammar =
        gb.repeat(NodeType.AND, gb.choice(orlist).or(negated),
                  1,
                  Integer.MAX_VALUE);
    return grammar;
  }
  /* +***************************************************************** */
  @Test
  public void testMiniLanguage() throws Exception {
    Parser<TestNode,Codes> grammar = miniLanguage();
    //System.out.printf("%s%n", ((Grammar)absimpa).lookaheadSet());
    TestNode node = analyze("- bla NOT aaa boar OR ey", grammar);
    assertEquals(NodeType.AND, node.getValue());
    assertEquals(NodeType.NOT, node.getChildType(0));
    assertEquals(NodeType.NOT, node.getChildType(1));
    assertEquals(NodeType.OR, node.getChildType(2));
  }
  /* +***************************************************************** */
  @Test
  public void testMiniLanguageORORThrows() throws Exception {
    Parser<TestNode,Codes> grammar = miniLanguage();

    EnumSet<Codes> expectedTokens = codeSet(Codes.TERM, Codes.SCOPE);
    checkParseException("abc:do OR OR zwei", grammar,
                        expectedTokens, Codes.OR);
  }
  /* +***************************************************************** */
  @Test
  public void testMiniLanguageSCOPESCOPEThrows() throws Exception {
    Parser<TestNode,Codes> grammar = miniLanguage();

    EnumSet<Codes> expectedTokens = codeSet(Codes.TERM);
    checkParseException("abc: abc:", grammar,
                        expectedTokens, Codes.SCOPE);
  }
  /* +***************************************************************** */
  @Test
  public void testMiniLanguageNOTNOTThrows() throws Exception {
    Parser<TestNode,Codes> grammar = miniLanguage();

    EnumSet<Codes> expectedTokens = codeSet(Codes.TERM, Codes.SCOPE);
    checkParseException("abc NOT NOT rest", grammar,
                        expectedTokens, Codes.NOT);
  }
  /* +***************************************************************** */
  @Test
  public void testMiniLanguageDanglingORThrows() throws Exception {
    Parser<TestNode,Codes> grammar = miniLanguage();

    EnumSet<Codes> expectedTokens = codeSet(Codes.TERM, Codes.SCOPE);
    checkParseException("abc OR", grammar,
                        expectedTokens, Codes.EOF);
  }
  /* +***************************************************************** */
  private EnumSet<Codes> codeSet(Codes ... codes) {
    EnumSet<Codes> result = EnumSet.<Codes>noneOf(Codes.class);
    for(Codes c : codes) result.add(c);
    return result;
  }
  /* +***************************************************************** */
  private void checkParseException(String text,
                                   Parser<TestNode,Codes> grammar,
                                   EnumSet<Codes> expected, Codes found)
    throws IOException
  {
    Exception ex = new Exception();
    try {
      analyze(text, grammar);
    } catch( ParseException ee ) {
      ex = ee;
      // e.printStackTrace();
    }
    assertTrue( ex instanceof ParseException );
    ParseException pe = (ParseException)ex;
    assertEquals(expected, pe.getExpectedTokenCodes());
    assertEquals(found, pe.getFoundTokenCode());
  }
  /*+******************************************************************/
  @Test 
  public void minimalRecursive() throws Exception {
    Grammar<TestNode,Codes> OP = gb.token(Codes.POPEN);
    Grammar<TestNode,Codes> CP = gb.token(Codes.PCLOSE);
    Recurse<TestNode,Codes> rec = new Recurse<TestNode,Codes>();
    
    Grammar<TestNode,Codes> parens = 
      gb.seq(OP).add(rec).add(CP);

    rec.setChild(gb.choice(term).or(parens));
    
    Parser<TestNode,Codes> p = parens.compile();
    TestNode node = analyze("(a)", p);
    //node.dump(System.out);
    
    assertEquals(3, node.numChildren());
    assertEquals(NodeType.TOKEN, node.getChildType(0));
    assertEquals(NodeType.TOKEN, node.getChildType(1));
    assertEquals(NodeType.TOKEN, node.getChildType(2));
    Codes[] codes = {Codes.POPEN, Codes.TERM, Codes.PCLOSE,};
    for(int i=0; i<3; i++) {
      LeafNode leaf = (LeafNode)(node.getChild(i));
      assertEquals(codes[i],leaf.t.getCode());
    }
    Exception e = null;
    try {
      analyze("((a b))", p);
    } catch(Exception ee) {
      e = ee;
    }
    assertTrue(e instanceof ParseException);
    String msg = e.getMessage();
    assertEquals("1:5:found token `TERM(b)' but expected `PCLOSE'", msg);
  }
  /*+******************************************************************/
  @Test
  public void leftRecurseImmediate() throws Exception {
    Recurse<TestNode,Codes> rec = new Recurse<TestNode,Codes>();
    Grammar<TestNode,Codes> seq = gb.seq(rec).add(term);
    rec.setChild(seq);
    
    Exception e = null;
    try {
      seq.compile();
    } catch( Exception ee) {
      e = ee;
    }
    assertTrue(e instanceof LeftRecursiveException);
    //System.out.printf("%s%n",e);
  }
  /*+******************************************************************/
  @Test
  public void leftRecurseWithAStar() throws Exception {
    Recurse<TestNode,Codes> rec = new Recurse<TestNode,Codes>();
    Grammar<TestNode,Codes> aStar = gb.repeat(term, 0, 1);
    Grammar<TestNode,Codes> seq = 
      gb.seq(aStar).add(rec).add(term).setName("TOP");
    rec.setChild(seq);
    
    Exception e = null;
    try {
      seq.compile();
    } catch( Exception ee) {
      e = ee;
    }
    assertTrue(e instanceof LeftRecursiveException);
    //System.out.printf("%s%n",e);
  }
  /*+******************************************************************/
  @Test
  public void optionalSeqStart() throws Exception {
    Grammar<TestNode,Codes> aStar =
      gb.repeat(term, 0, 1);
    Grammar<TestNode,Codes> bStar =
      gb.repeat(scopename, 0, 1);

    Grammar<TestNode,Codes> optSeq =
      gb.seq(aStar).add(bStar).add(not);
    
    Parser<TestNode,Codes> p = optSeq.compile();
    
    TestNode node = analyze("NOT", p);
    //node.dump(System.out);
    assertEquals(1, node.numChildren());
    assertEquals(NodeType.TOKEN, node.getChildType(0));
  }
  /*+******************************************************************/
  @Test
  public void optionalSeq() throws Exception {
    Grammar<TestNode,Codes> aStar =
      gb.repeat(term, 0, 1);
    Grammar<TestNode,Codes> bStar =
      gb.repeat(scopename, 0, 1);
    Grammar<TestNode,Codes> optSeq =
      gb.seq(aStar).add(bStar);
    
    Grammar<TestNode,Codes> ctop = gb.choice(optSeq).or(not);
    Parser<TestNode,Codes> p = ctop.compile();
    
    TestNode node = analyze("NOT", p);
    //node.dump(System.out);
    assertEquals(NodeType.TOKEN, node.getValue());
    
    Grammar<TestNode,Codes> seqtop = gb.seq(optSeq).add(not);
    p = seqtop.compile();
    node = analyze("NOT", p);
    //node.dump(System.out);
    assertEquals(1, node.numChildren());
    assertEquals(NodeType.TOKEN, node.getChildType(0));
  }
  /*+******************************************************************/
  @Test
  public void repeatOfOptional() throws Exception {
    // REMINDER: an optional sub-grammar can satisfy all numbers of repeats
    Grammar<TestNode,Codes> optTerm = gb.repeat(term, 0, 1);
    Grammar<TestNode,Codes> terms = gb.repeat(optTerm, 2, 5);
    Grammar<TestNode,Codes> top = gb.seq(terms).add(scopename);
    
    Parser<TestNode,Codes> p = top.compile();
    TestNode node = analyze("a b n:", p);

    //node.dump(System.out);
    assertEquals(3, node.numChildren());
    assertEquals(NodeType.TOKEN, node.getChildType(0));
    assertEquals(NodeType.TOKEN, node.getChildType(1));
    assertEquals(NodeType.TOKEN, node.getChildType(2));

    node = analyze("a n:", p);
    assertEquals(NodeType.TOKEN, node.getChildType(0));
    assertEquals(NodeType.TOKEN, node.getChildType(1));

    node = analyze("n:", p);
    assertEquals(NodeType.TOKEN, node.getChildType(0));
  
  }
  /* +***************************************************************** */
  @Test
  public void repeatThrows() throws Exception
  {
    Exception e = new Exception();
    try {
      gb.repeat(term, 1, 0);
    } catch( IllegalArgumentException ee ) {
      e = ee;
    }
    assertEquals("java.lang.IllegalArgumentException", 
                 e.getClass().getName());
  }
  /*+******************************************************************/
  @Test
  public void explicitEOFMatching() throws Exception {
    Grammar<TestNode,Codes> minig = miniLanguageGrammar();
    Grammar<TestNode,Codes> g = gb.seq(minig).add(gb.token(Codes.EOF));

    Parser<TestNode,Codes> p = g.compile();
    TestNode node = analyze("a b n: a", p);

    assertEquals(2, node.numChildren());
    assertEquals(NodeType.AND, node.getChildType(0));
    assertEquals(NodeType.TOKEN, node.getChildType(1));

    node = analyze("a OR b", p);
    assertEquals(2, node.numChildren());
    assertEquals(NodeType.AND, node.getChildType(0));
    assertEquals(NodeType.TOKEN, node.getChildType(1));
  }
  /*+******************************************************************/
}
