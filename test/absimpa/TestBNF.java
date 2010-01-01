package absimpa;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import absimpa.wisent.BNF;
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
    Grammar<String,Token> h = bnf.rule("expr", "IDENT | NUMBER");
    
    //System.out.println(g);
    assertEquals("assign->(IDENT EQUAL [expr->])", g.toString());
    
    //System.out.println(h);
    assertEquals("expr->(IDENT | NUMBER)", h.toString());
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
}
