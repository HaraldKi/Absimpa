package absimpa;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import example.ExprLanguage.*;

public class TestExprLanguage {
  L lex = null;
  Eparser parser = null;
  
  @Before
  public void setUp() {
    lex = example.ExprLanguage.createLexer();
    parser = example.ExprLanguage.createParser();
  }
  private Expr analyze(String text) throws IOException, ParseException {
    lex.initAnalysis(text);
    return parser.parse(lex);
  }
  /*+******************************************************************/
  private double eval(String text) throws IOException, ParseException {
    return analyze(text).eval();
  }
  /*+******************************************************************/
  @Test
  public void addTest() throws Exception {
    double eps = 1e-20;
    Expr n = analyze("3 + 4  + 5 - 5");
    assertEquals(7.0, n.eval(), eps);

    assertEquals(-6.0, eval("2*-3"), eps);
    assertEquals(2.5, eval("10/4"), eps);
    assertEquals(2.0, eval("10/4 - 1/2"), eps);
    assertEquals(1.0, eval("(3- 2)"), eps);
    assertEquals(1.0, eval("(3- 2)*(-1+ 2)"), eps);
    assertEquals(1.0, eval("(1)*(1)- 0"), eps);
    assertEquals(11.0, eval("(((12))) - 1"), eps);
    assertEquals(6.0, eval("3*(1+ 1* (4- 3))"), eps);
  }
  /*+******************************************************************/

}
