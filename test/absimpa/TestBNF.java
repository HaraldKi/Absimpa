package absimpa;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestBNF {
  private static enum Token {
    IDENT, NUMBER, OPAREN, CPAREN, PLUS, MINUS, TIMES, DIVIDE, EQUAL;
  }
  /*+******************************************************************/
  BNF<String,Token> bnf = null;
  /*+******************************************************************/
  @Before
  public void setup() {
    bnf = new BNF<>(Token.class);
  }
  /*+******************************************************************/
  private static final class NF implements NodeFactory<String> {
    private final String name;
    public NF(String name) { this.name = name; }

    @Override
    public String create(List<String> children) {
      return null;
    }
    public String toString() {
      return '%'+name;
    }
  }
  /*+******************************************************************/
  @Test
  public void minimalTest() throws Exception {
    Grammar<String,Token> g = bnf.rule("R", "IDENT");
    assertEquals("R", g.toString());
    assertEquals("R(IDENT)", g.ruleString());

    char[] opts = {'+','*','?'};
    for(char c : opts) {
      g = bnf.rule("S"+c, "IDENT"+c);
      assertEquals("R"+c, g.ruleString());
    }
    
    g = bnf.rule("T", "IDENT EQUAL NUMBER");
    assertEquals("(R EQUAL NUMBER)", g.ruleString());
    
    g = bnf.rule("U", "%(NUMBER (TIMES NUMBER)*)", new NF("prod"));
    assertEquals("%prod(NUMBER (TIMES NUMBER)*)", g.ruleString());
    
    //System.out.println(g);
  }
  /*+******************************************************************/
  @Test
  public void placeholderUndefinedTest() {
    try {
      bnf.rule("assign", "IDENT EQUAL expr");
      fail("expected exception");
    } catch( Exception e ) {
      assertTrue(e instanceof ParseException );
      assertTrue(e.getMessage().endsWith("undefined grammar element"));
    }
  }
  /*+******************************************************************/
  @Test
  public void placeholderTest() throws Exception {
    bnf.rule("expr");
    Grammar<String,Token> g = bnf.rule("assign", "IDENT EQUAL expr");
    Grammar<String,Token> m = bnf.rule("m", "expr | NUMBER");
    Grammar<String,Token> h = bnf.rule("expr", "IDENT | NUMBER");
    Grammar<String,Token> n = bnf.rule("n", "expr | NUMBER");
    
    //System.out.println(g);
    assertEquals("(IDENT EQUAL r#expr)", g.ruleString());
    
    //System.out.println(h);
    assertEquals("(IDENT | NUMBER)", 
                 ((Recurse<String,Token>)h).children().iterator().next().ruleString());
    
    //System.out.println(m);
    assertEquals("(r#expr | NUMBER)", m.ruleString());
    
    //System.out.println(n);
    String sm = m.toString().substring(1);
    String sn = n.toString().substring(1);
    assertEquals(sn, sm);
  }
  /*+******************************************************************/
  @Test
  public void twoNFtest() throws Exception {
    Grammar<String,Token> g;
    g = bnf.rule("R", "%(IDENT NUMBER) | %(MINUS PLUS)",
                 new NF("pair1"), new NF("pair2"));
    //System.out.println(g);
    assertEquals("(%pair1(IDENT NUMBER) | %pair2(MINUS PLUS))", 
                 g.ruleString());
  }
  /*+******************************************************************/
  @Test
  public void rangeTest() throws Exception {
    Grammar<String,Token> g;
    g = bnf.rule("R", "IDENT{2,7}");
    //System.out.println(g);
    assertEquals("IDENT{2,7}", g.ruleString());
    
    g = bnf.rule("S", "NUMBER{3}");
    //System.out.println(g);
    assertEquals("NUMBER{3,3}", g.ruleString());
    
    g = bnf.rule("T", "EQUAL{0,*}");
    //System.out.println(g);
    assertEquals("EQUAL*", g.ruleString());

    g = bnf.rule("U", "EQUAL{3,*}");
    //System.out.println(g);
    assertEquals("EQUAL{3,*}", g.ruleString());
  }
  /*+******************************************************************/
  @Test
  public void rangeTooLarge() {
    Throwable ee = new Throwable();
    try {
      bnf.rule("R", "IDENT{2,777777777777777777777777777777}");
    } catch( ParseException e) {
      ee = e.getCause();
    }
    assertEquals(NumberFormatException.class, ee.getClass());
  }
  /*+******************************************************************/
  @Test 
  public void missingNodeFactory() {
    Exception ee = new Exception();
    try {
      bnf.rule("R", "%(IDENT)");
    } catch( ParseException e ) {
      ee = e;
    }
    assertTrue(ee.getMessage().endsWith("not enough node factories"));
  }
  /*+******************************************************************/
  @Test(expected=IllegalArgumentException.class)
  public void multiplyDefinedName() throws Exception {
    bnf.rule("a", "IDENT");
    bnf.rule("a", "NUMBER");
  }
  /*+******************************************************************/
  @Test
  public void testGetGrammar() throws Exception {
    Grammar<String,Token> g = bnf.rule("assign", "IDENT EQUAL NUMBER");
    assertEquals(g, bnf.getGrammar("assign"));
  }
  /*+******************************************************************/
  @Test
  public void manyNodeFactories() throws Exception {
    @SuppressWarnings("unchecked")
    Grammar<String,Token> g = 
      bnf.rule("a", "%(IDENT | %(NUMBER) | %(EQUAL))",
               new NF("all"), new NF("num"), new NF("="));
    //System.out.println(g.ruleString());
    assertEquals("%all(IDENT | %num(NUMBER) | %=(EQUAL))", g.ruleString());
  }
  /*+******************************************************************/
  @Test(expected=LeftRecursiveException.class)
  public void leftrecursiveLoop() throws Exception {
    bnf.rule("b");
    bnf.rule("a", "b IDENT");
    bnf.rule("b", "a EQUAL NUMBER");
    bnf.compile("a");
  }
  /*+******************************************************************/
}
