package absimpa;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestBNF {
  private static enum Token {
    IDENT, NUMBER, OPAREN, CPAREN, PLUS, MINUS, TIMES, DIVIDE, EQUAL;
  }
  /*+******************************************************************/
  BNF<String,Token> bnf = null;
  /*+******************************************************************/
  @Before
  public void setup() {
    bnf = new BNF<String,Token>(Token.class);
  }
  /*+******************************************************************/
  private final class NF implements NodeFactory<String> {
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
    assertEquals("R->IDENT", g.toString());

    char[] opts = {'+','*','?'};
    for(char c : opts) {
      g = bnf.rule("S"+c, "IDENT"+c);
      assertEquals("S"+c+"->(R->IDENT)"+c, g.toString());
    }
    
    g = bnf.rule("T", "IDENT EQUAL NUMBER");
    assertEquals("T->(R->IDENT EQUAL NUMBER)", g.toString());
    
    g = bnf.rule("U", "%(NUMBER (TIMES NUMBER)*)", new NF("prod"));
    assertEquals("U->%prod(NUMBER (TIMES NUMBER)*)", g.toString());
    
    //System.out.println(g);
  }
  /*+******************************************************************/
  @Test
  public void placeholderTest() throws Exception {
    Grammar<String,Token> g = bnf.rule("assign", "IDENT EQUAL expr");
    Grammar<String,Token> m = bnf.rule("m", "expr | NUMBER");
    Grammar<String,Token> h = bnf.rule("expr", "IDENT | NUMBER");
    Grammar<String,Token> n = bnf.rule("n", "expr | NUMBER");
    
    System.out.println(g);
    assertEquals("assign->(IDENT EQUAL r#expr->[expr->])", g.toString());
    
    System.out.println(h);
    assertEquals("expr->(IDENT | NUMBER)", h.toString());
    
    System.out.println(m);
    assertEquals("m->([expr->] | NUMBER)", m.toString());
    
    System.out.println(n);
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
    assertEquals("R->(%pair1(IDENT NUMBER) | %pair2(MINUS PLUS))", 
                 g.toString());
  }
  /*+******************************************************************/
  @Test
  public void rangeTest() throws Exception {
    Grammar<String,Token> g;
    g = bnf.rule("R", "IDENT{2,7}");
    //System.out.println(g);
    assertEquals("R->(IDENT){2,7}", g.toString());
    
    g = bnf.rule("S", "NUMBER{3}");
    //System.out.println(g);
    assertEquals("S->(NUMBER){3,3}", g.toString());
    
    g = bnf.rule("T", "EQUAL{0,*}");
    //System.out.println(g);
    assertEquals("T->(EQUAL)*", g.toString());

    g = bnf.rule("U", "EQUAL{3,*}");
    //System.out.println(g);
    assertEquals("U->(EQUAL){3,*}", g.toString());
  }
  /*+******************************************************************/
  @Test
  public void rangeTooLarge() throws Exception {
    Throwable ee = null;
    try {
      bnf.rule("R", "IDENT{2,777777777777777777777777777777}");
    } catch( ParseException e) {
      ee = e.getCause();
    }
    assertEquals(NumberFormatException.class, ee.getClass());
  }
  /*+******************************************************************/
  @Test 
  public void missingNodeFactory() throws Exception {
    Exception ee = null;
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
    Grammar<String,Token> g = bnf.rule("assign", "IDENT EQUALS NUMBER");
    assertEquals(g, bnf.getGrammar("assign"));
  }
  /*+******************************************************************/
  @Test
  public void manyNodeFactories() throws Exception {
    @SuppressWarnings("unchecked")
    Grammar<String,Token> g = 
      bnf.rule("a", "%(IDENT | %(NUMBER) | %(EQUAL))",
               new NF("all"), new NF("num"), new NF("="));
    //System.out.println(g);
    assertEquals("a->%all(IDENT | %numNUMBER | %=EQUAL)", g.toString());
  }
  /*+******************************************************************/
  @Test(expected=IllegalStateException.class)
  public void missingDefinitions() throws Exception {
    bnf.rule("a", "IDENT | expr");
    bnf.compile("a");    
  }
  /*+******************************************************************/
  @Test(expected=LeftRecursiveException.class)
  public void leftrecursiveLoop() throws Exception {
    bnf.rule("a", "b IDENT");
    bnf.rule("b", "a EQUAL NUMBER");
    bnf.compile("a");
  }
  /*+******************************************************************/
}
