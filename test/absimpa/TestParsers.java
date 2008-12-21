package absimpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


import java.io.IOException;
import java.util.*;

import nodes.Node;
import nodes.ValueNode;

import org.junit.Before;
import org.junit.Test;

import absimpa.MostTrivialLexer.TestToken;


public class TestParsers {
  private static class L extends MostTrivialLexer<Codes> {
    public L(Codes eofCode) {
      super(eofCode);
    }    
    @Override
    public L addToken(Codes tc, String regex) {
      super.addToken(tc, regex);
      return this;
    }
  }

  private L lex;
  private TokenGrammar<TestNode,Codes,L> term;
  private TokenGrammar<TestNode,Codes,L> and;
  private TokenGrammar<TestNode,Codes,L> or;
  private TokenGrammar<TestNode,Codes,L> not;
  private TokenGrammar<TestNode,Codes,L> scopename;

  private final GrammarBuilder<TestNode,Codes,L> gb = 
    new GrammarBuilder<TestNode,Codes,L>(NodeType.DEFAULT);

  private static enum Codes {
    SCOPE, TERM, AND, OR, POPEN, PCLOSE, NOT, EOF;
  }
  /* +***************************************************************** */
  private static enum NodeType
      implements NodeFactory<TestNode>, LeafFactory<TestNode,Codes,L> {
    DEFAULT, TOKEN, AND, OR, ORS, ORP, SCOPE, TERMLIST, SEQ, NOT, OPENPAREN, CLOSEPAREN;

    public TestNode create(List<TestNode> children) {
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
    public TestNode create(L lex) {
      return new LeafNode(lex.currentToken());
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
  }
  /*+******************************************************************/
  private static class LeafNode extends TestNode {
    private final TestToken t;
    @SuppressWarnings("unchecked")
    public LeafNode(TestToken t) {
      super(NodeType.TOKEN, Collections.EMPTY_LIST);
      this.t = t;
    }
    public TestToken getToken() {
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
    term = gb.token(NodeType.DEFAULT,Codes.TERM);
    and = gb.token(NodeType.DEFAULT,Codes.AND);
    or = gb.token(NodeType.DEFAULT,Codes.OR);
    not = gb.token(NodeType.DEFAULT,Codes.NOT);
    scopename = gb.token(NodeType.DEFAULT,Codes.SCOPE);

  }
  /* +***************************************************************** */
  /* +***************************************************************** */
  @Test
  public void testTokenParser() throws Exception
  {
    Parser<TestNode,Codes,L> tp =
        gb.token(NodeType.DEFAULT, Codes.TERM).compile();
    lex.initAnalysis("abc ddd: ");
    checkTokenParse(tp, "abc", Codes.TERM);
  }
  private void checkTokenParse(Parser<TestNode,Codes,L> tp, String text,
                               Codes code) throws Exception
  {
    TestNode node = tp.parse(lex);
    //node.dump(System.out);
    assertTrue(node instanceof LeafNode);
    LeafNode n = (LeafNode)node;
    TestToken t = n.getToken();
    assertEquals(text, t.getText());
    assertEquals(code, t.getCode());
  }
  /* +***************************************************************** */
  @Test
  public void testSequenceParser() throws Exception {
    @SuppressWarnings("unchecked")
    Parser<TestNode,Codes,L> scope = 
      gb.seq(NodeType.SCOPE, scopename).add(term).compile();

    lex.initAnalysis("name: value");
    checkSequenceParse(scope, NodeType.SCOPE, NodeType.TOKEN, NodeType.TOKEN);
  }
  private void checkSequenceParse(Parser<TestNode,Codes,L> scope,
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
  private TestNode analyze(String text, Parser<TestNode,Codes,L> p)
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
    Parser<TestNode,Codes,L> rep = makeRepeatGrammar(term, 2,2).compile();
    
    TestNode node = analyze("eins zwei drei vier", rep);
    assertEquals(NodeType.TERMLIST, node.getValue());
    assertEquals(NodeType.TOKEN, node.getChildType(0));
    assertEquals(NodeType.TOKEN, node.getChildType(1));
    assertEquals(2, node.numChildren());
  }
  @Test
  public void testRepeat0With0() throws Exception {
    Repeat<TestNode,Codes,L> rep = makeRepeatGrammar(scopename, 0,2);

    Grammar<TestNode,Codes,L> seq = gb.seq(NodeType.SEQ, rep).add(term);

    TestNode node = analyze("abc: xyz", seq.compile());

    assertEquals(NodeType.TERMLIST, node.getChildType(0));
    assertEquals(NodeType.TOKEN, node.getChildType(1));
    assertEquals(2, node.numChildren());
  }
  @Test
  public void testRepeatStopNonmatchingToken() throws Exception {
    Repeat<TestNode,Codes,L> rep = makeRepeatGrammar(term, 0, 5);

    TestNode node = analyze("eins zwei ende:", rep.compile());

    assertEquals(NodeType.TOKEN, node.getChildType(0));
    assertEquals(NodeType.TOKEN, node.getChildType(1));
    assertEquals(2, node.numChildren());

  }
  @Test
  public void testRepeatStopAtMax() throws Exception {
    Repeat<TestNode,Codes,L> rep = makeRepeatGrammar(term, 0, 2);

    TestNode node = analyze("eins zwei drei vier", rep.compile());

    assertEquals(NodeType.TOKEN, node.getChildType(0));
    assertEquals(NodeType.TOKEN, node.getChildType(1));
    assertEquals(2, node.numChildren());
  }
  @Test(expected=ParseException.class)
  public void testRepeatNotEnough() throws Exception {
    Repeat<TestNode,Codes,L> rep = makeRepeatGrammar(term, 4, 4);

    TestNode node = analyze("eins zwei ", rep.compile());

    assertEquals(NodeType.TOKEN, node.getChildType(0));
    assertEquals(NodeType.TOKEN, node.getChildType(1));
    assertEquals(2, node.numChildren());
  }
  private Repeat<TestNode,Codes,L> makeRepeatGrammar(Grammar<TestNode,Codes,L> g,
                                                     int min, int max)
  {
    Repeat<TestNode,Codes,L> rep = gb.repeat(NodeType.TERMLIST, g, min, max);
    return rep;
  }
  /*+******************************************************************/
  @Test
  public void testChoice() throws Exception {
    Parser<TestNode,Codes,L> choice = makeChoiceParser().compile();
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
    Parser<TestNode,Codes,L> choice = makeChoiceParser().compile();
    analyze("", choice);
  }
  private Enum getTokenCode(TestNode node) {
    LeafNode ln = (LeafNode)node;
    return ln.getToken().getCode();
  }
  private Choice<TestNode,Codes,L> makeChoiceParser() {
    Choice<TestNode,Codes,L> choice =
      gb.choice(term)
      .or(and)
      .or(or);
    return choice;
  }
  /*+******************************************************************/
  @Test
  public void testWithOptionalChoice() throws Exception {
    Grammar<TestNode,Codes,L> repeatOptional = makeRepeatGrammar(term, 0, 1);

    Choice<TestNode,Codes,L> choice =
      gb.choice(and).or(repeatOptional).or(or);

    TestNode node = analyze("ascope:", choice.compile());
    
    assertEquals(null, node);
  }
  /*+******************************************************************/
  @Test
  public void SequenceInChoice() throws Exception {

    Sequence<TestNode,Codes,L> seq = 
      gb.seq(NodeType.SCOPE, scopename).add(term);

    Choice<TestNode,Codes,L> choice = gb.choice(or).or(seq);

    TestNode node = analyze("abc: bla", choice.compile());
    
    assertEquals(NodeType.SCOPE, node.getValue());
    assertEquals(2, node.numChildren());
  }
  /* +***************************************************************** */
  @Test 
  public void notEnoughRepeat() throws Exception {
    Grammar<TestNode,Codes,L> rep =
      gb.repeat(NodeType.AND, term, 4, 1000);
    EnumSet<Codes> expected = codeSet(Codes.TERM);
    checkParseException("abc abc", rep.compile(), expected, Codes.EOF);
  }
  /* +***************************************************************** */
  @Test
  public void optionalInChoice() throws Exception {
    // optTerm -> term?
    Grammar<TestNode,Codes,L> optTerm =
      gb.repeat(NodeType.AND, term, 0, 1);

    // critChoice -> optTerm | and
    Grammar<TestNode,Codes,L> critChoice = 
      gb.choice(optTerm).or(and);

    // absimpa -> critChoice or
    Grammar<TestNode,Codes,L> grammar = 
      gb.seq(NodeType.SEQ, critChoice).add(or);

    TestNode node = analyze("OR", grammar.compile());
    //node.dump(System.out);
    assertEquals(NodeType.SEQ, node.getValue());
    assertEquals(NodeType.TOKEN, node.getChildType(0));
  }
  /* +***************************************************************** */
  @Test
  public void conflictingChoice() throws Exception
  {
    Grammar<TestNode,Codes,L> scope =
        gb.seq(NodeType.SCOPE, scopename).add(term);

    Grammar<TestNode,Codes,L> negatedTerm =
        gb.seq(NodeType.NOT, not).add(term);

    Grammar<TestNode,Codes,L> negatedScope =
        gb.seq(NodeType.NOT, not).add(scope);

    Exception e = null;
    try {
      @SuppressWarnings("unchecked")
      Parser<TestNode,Codes,L> p =
          gb.choice(negatedScope).or(negatedTerm).compile();
      assertEquals(null, p);
    } catch( Exception ee ) {
      e = ee;
    }

    assertTrue(e.getMessage()
        .startsWith("conflicting lookahead [NOT] for grammars"));
  }
  /* +***************************************************************** */
  @Test
  public void conflictSequenceLA() throws Exception
  {
    Grammar<TestNode,Codes,L> optTerm = gb.repeat(term, 0, 1);

    Exception e = null;
    try {
      @SuppressWarnings("unchecked")
      Parser<TestNode,Codes,L> seq = 
        gb.seq(NodeType.AND, optTerm).add(term).compile();
      assertEquals(null, seq);
    } catch( Exception ee ) {
      e = ee;
    }
    //System.out.printf("%s%n", e.getMessage());
    assertTrue(e.getMessage()
        .startsWith("conflicting lookahead [TERM] for grammars"));

  }
  /* +***************************************************************** */
  @SuppressWarnings("unchecked")
  private Parser<TestNode,Codes,L> miniLanguage()  {
    Grammar<TestNode,Codes,L> scope = gb.seq(NodeType.SCOPE, scopename).add(term);
    Grammar<TestNode,Codes,L> literal = gb.choice(term).or(scope);

    Grammar<TestNode,Codes,L> orlist =
        gb.seq(NodeType.OR, literal)
        .add(gb.repeat(gb.seq(or).add(literal), 0, Integer.MAX_VALUE));

    Grammar<TestNode,Codes,L> negated = gb.seq(NodeType.NOT, not).add(literal);

    Grammar<TestNode,Codes,L> grammar =
        gb.repeat(NodeType.AND, gb.choice(orlist).or(negated),
                  1,
                  Integer.MAX_VALUE);
    return grammar.compile();
  }
  /* +***************************************************************** */
  @Test
  public void testMiniLanguage() throws Exception {
    Parser<TestNode,Codes,L> grammar = miniLanguage();
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
    Parser<TestNode,Codes,L> grammar = miniLanguage();

    EnumSet<Codes> expectedTokens = codeSet(Codes.TERM, Codes.SCOPE);
    checkParseException("abc:do OR OR zwei", grammar,
                        expectedTokens, Codes.OR);
  }
  /* +***************************************************************** */
  @Test
  public void testMiniLanguageSCOPESCOPEThrows() throws Exception {
    Parser<TestNode,Codes,L> grammar = miniLanguage();

    EnumSet<Codes> expectedTokens = codeSet(Codes.TERM);
    checkParseException("abc: abc:", grammar,
                        expectedTokens, Codes.SCOPE);
  }
  /* +***************************************************************** */
  @Test
  public void testMiniLanguageNOTNOTThrows() throws Exception {
    Parser<TestNode,Codes,L> grammar = miniLanguage();

    EnumSet<Codes> expectedTokens = codeSet(Codes.TERM, Codes.SCOPE);
    checkParseException("abc NOT NOT rest", grammar,
                        expectedTokens, Codes.NOT);
  }
  /* +***************************************************************** */
  @Test
  public void testMiniLanguageDanglingORThrows() throws Exception {
    Parser<TestNode,Codes,L> grammar = miniLanguage();

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
                                   Parser<TestNode,Codes,L> grammar,
                                   EnumSet<Codes> expected, Codes found)
    throws IOException
  {
    ParseException e = null;
    try {
      analyze(text, grammar);
    } catch( ParseException ee ) {
      e = ee;
      // e.printStackTrace();
    }
    assertEquals(expected, e.getExpectedTokenCodes());
    assertEquals(found, e.getFoundTokenCode());
  }
  /*+******************************************************************/
  @Test 
  public void minimalRecursive() throws Exception {
    Grammar<TestNode,Codes,L> OP = gb.token(NodeType.DEFAULT,Codes.POPEN);
    Grammar<TestNode,Codes,L> CP = gb.token(NodeType.DEFAULT,Codes.PCLOSE);
    Recurse<TestNode,Codes,L> rec = new Recurse<TestNode,Codes,L>();
    
    Grammar<TestNode,Codes,L> parens = 
      gb.seq(OP).add(rec).add(CP);

    rec.setChild(gb.choice(term).or(parens));
    
    Parser<TestNode,Codes,L> p = parens.compile();
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
      node = analyze("((a b))", p);
    } catch(ParseException ee) {
      e = ee;
    }
    assertTrue(e instanceof ParseException);
    String msg = e.getMessage();
    //System.out.printf("%s%n", msg);
    assertEquals("1:5:found token `TERM(b)' but expected one of [PCLOSE]", msg);
  }
  /*+******************************************************************/
  @Test
  public void leftRecurseImmediate() throws Exception {
    Recurse<TestNode,Codes,L> rec = new Recurse<TestNode,Codes,L>();
    Grammar<TestNode,Codes,L> seq = gb.seq(rec).add(term);
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
    Recurse<TestNode,Codes,L> rec = new Recurse<TestNode,Codes,L>();
    Grammar<TestNode,Codes,L> aStar = gb.repeat(term, 0, 1);
    Grammar<TestNode,Codes,L> seq = 
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
    Grammar<TestNode,Codes,L> aStar =
      gb.repeat(term, 0, 1);
    Grammar<TestNode,Codes,L> bStar =
      gb.repeat(scopename, 0, 1);

    Grammar<TestNode,Codes,L> optSeq =
      gb.seq(aStar).add(bStar).add(not);
    
    Parser<TestNode,Codes,L> p = optSeq.compile();
    
    TestNode node = analyze("NOT", p);
    //node.dump(System.out);
    assertEquals(1, node.numChildren());
    assertEquals(NodeType.TOKEN, node.getChildType(0));
  }
  /*+******************************************************************/
  @Test
  public void optionalSeq() throws Exception {
    Grammar<TestNode,Codes,L> aStar =
      gb.repeat(term, 0, 1);
    Grammar<TestNode,Codes,L> bStar =
      gb.repeat(scopename, 0, 1);
    Grammar<TestNode,Codes,L> optSeq =
      gb.seq(aStar).add(bStar);
    
    Grammar<TestNode,Codes,L> ctop = gb.choice(optSeq).or(not);
    Parser<TestNode,Codes,L> p = ctop.compile();
    
    TestNode node = analyze("NOT", p);
    //node.dump(System.out);
    assertEquals(NodeType.TOKEN, node.getValue());
    
    Grammar<TestNode,Codes,L> seqtop = gb.seq(optSeq).add(not);
    p = seqtop.compile();
    node = analyze("NOT", p);
    //node.dump(System.out);
    assertEquals(1, node.numChildren());
    assertEquals(NodeType.TOKEN, node.getChildType(0));
  }
  /*+******************************************************************/
  @Test
  public void repeatOfOptional() throws Exception {
    Grammar<TestNode,Codes,L> optTerm = gb.repeat(term, 0, 1);
    Grammar<TestNode,Codes,L> terms = gb.repeat(optTerm, 2, 3);
    Grammar<TestNode,Codes,L> top = gb.seq(terms).add(scopename);
    
    Parser<TestNode,Codes,L> p = top.compile();
    TestNode node = analyze("a b n:", p);

    //node.dump(System.out);
    assertEquals(3, node.numChildren());
    assertEquals(NodeType.TOKEN, node.getChildType(0));
    assertEquals(NodeType.TOKEN, node.getChildType(1));
    assertEquals(NodeType.TOKEN, node.getChildType(2));

    Exception e = null;
    try {
      node = analyze("a n:", p);
    } catch( ParseException ee) {
      e = ee;
    }
    String msg = e.getMessage();
    //System.out.printf("%s%s",msg);
    assertEquals("1:3:found token `SCOPE(n:)' but expected one of [TERM]", msg);
  }
  /* +***************************************************************** */
  @Test
  public void repeatThrows() throws Exception
  {
    Exception e = null;
    try {
      gb.repeat(term, 1, 0);
    } catch( IllegalArgumentException ee ) {
      e = ee;
    }
    assertEquals("java.lang.IllegalArgumentException", 
                 e.getClass().getName());
  }
}
