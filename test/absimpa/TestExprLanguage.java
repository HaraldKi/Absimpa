package absimpa;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import absimpa.lexer.SimpleLexer;
import example.ExprLanguage.*;

public class TestExprLanguage {
  SimpleLexer<Expr,Codes> lex = null;
  Eparser parser = null;

  @Before
  public void setUp() {
    lex = example.ExprLanguage.createLexer();
    parser = example.ExprLanguage.createParser();
  }
  private Expr analyze(String text) throws ParseException {
    lex.initAnalysis(text);
    return parser.parse(lex);
  }
  /*+******************************************************************/
  private double eval(String text) throws ParseException {
    Expr result = analyze(text);
    double x = result.value().doubleValue();
    return x;
  }
  /*+******************************************************************/
  @Test
  public void addTest() throws Exception {
    double eps = 1e-20;
    Expr n = analyze("3+4+5-6-8");

    assertEquals(-2.0, n.value().doubleValue(), eps);

    assertEquals(-6.0, eval("2*-3"), eps);
    assertEquals(2.5, eval("10/4"), eps);
    assertEquals(2.0, eval("10/4 - 1/2"), eps);
    assertEquals(1.0, eval("(3- 2)"), eps);
    assertEquals(1.0, eval("(3- 2)*(-1+ 2)"), eps);
    assertEquals(1.0, eval("(1)*(1)- 0"), eps);
    assertEquals(11.0, eval("(((12))) - 1"), eps);
    assertEquals(6.0, eval("3*(1+ 1* (4- 3))"), eps);
    assertEquals(-6.0, eval("3*-3--3"), eps);
    assertEquals(6.0, eval("3*+3-+3"), eps);

    assertEquals(1.0, eval("+1"), eps);
  }
  /*+******************************************************************/

}
