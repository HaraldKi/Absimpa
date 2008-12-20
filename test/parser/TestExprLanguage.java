package parser;

import static org.junit.Assert.assertEquals;

import grammar.*;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import parser.MostTrivialLexer.TestToken;

/**
 * create a simple expression language and use it for testing.
 *
 */
public class TestExprLanguage {
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
  
  private static enum Codes {
    EOF, NUMBER, PLUS, MINUS, TIMES, DIVIDE, OPAREN, CPAREN;
  }
  L lex = null;
  Parser<Expr,Codes,L> parser = null;
  /*+******************************************************************/
  private static final class Expr {
    public final Op ecode;
    private final List<Expr> children;
    private final Number value;
    public Expr(Number value) {
      this.value = value;
      this.children = null;
      this.ecode = Op.NUMBER;
    }
    public Expr(Op code, List<Expr> children) {
      this.ecode = code;
      this.children = children;
      this.value = null;
    }
    public List<Expr> getChildren() {
      return children;
    }
    public void dump(Appendable out, String indent) throws IOException {
      out.append(indent);
      if( ecode==Op.NUMBER ) {
        out.append(value.toString()).append("\n");
      } else {
        String N = Integer.toString(children.size());
        out.append(ecode.toString()).append(N).append("\n");
        for(Expr e : children) {
          e.dump(out, "  "+indent);
        }
      }
    }
    public double eval() {
      switch(ecode) {
      case NUMBER: 
        return value.doubleValue();
      case PRODUCT:
      case SUM:
        return evalList(0.0, children);
      case TIMES:
      case DIVIDE:
      case PLUS:
      case MINUS: 
        return children.get(0).eval();
      default: throw new RuntimeException("should not happen");
      }
    }
    private static double evalList(double start, List<Expr> l) {
      for(Expr e : l) {
        double operand = e.eval();
        switch(e.ecode) {
        case TIMES: start = start * operand; break;
        case DIVIDE: start = start / operand; break;
        case PLUS: start = start + operand; break;
        case MINUS: start = start - operand; break;
        case SUM:
        case PRODUCT:
        case NUMBER: 
          start = operand; 
          break;
        default: throw new RuntimeException("should not happen: "+e.ecode);
        }
      }
      return start;
    }
  }
  /*+******************************************************************/
  @Before
  public void setUp() {
    setUpLex();
    setUpParser();
  }
  public void setUpLex() {
    lex = new L(Codes.EOF)
    .addToken(Codes.NUMBER, "[+-]?[1-9][0-9]*|0")
    .addToken(Codes.PLUS, "[+]")
    .addToken(Codes.MINUS, "-")
    .addToken(Codes.TIMES, "[*]")
    .addToken(Codes.DIVIDE, "[/]")
    .addToken(Codes.OPAREN, "[(]")
    .addToken(Codes.CPAREN, "[)]")
    ;    
  }
  public void setUpParser() {
    // term -> NUMBER | expr 
    // product -> term ( (mul | div ) term)*
    // sum -> product ( (add | sub ) product)*
    // expr -> '(' sum ')'
    GrammarBuilder<Expr, Codes,L> gb =
      new GrammarBuilder<Expr,Codes,L>(Op.DEFAULT);

    Grammar<Expr,Codes,L> number = gb.token(Op.NUMBER, Codes.NUMBER);
    Recurse<Expr,Codes,L> recExpr = new Recurse<Expr,Codes,L>();
    Grammar<Expr,Codes,L> term = gb.choice(number).or(recExpr);

    Grammar<Expr,Codes,L> timesNum = 
      gb.seq(Op.TIMES, gb.token(Op.DEFAULT,Codes.TIMES)).add(term);

    Grammar<Expr,Codes,L> divideNum = 
      gb.seq(Op.DIVIDE, gb.token(Op.DEFAULT,Codes.DIVIDE)).add(term);
    
    Grammar<Expr,Codes,L> product = 
      gb.seq(Op.PRODUCT, term)
      .add(gb.star(gb.choice(timesNum).or(divideNum)));


    Grammar<Expr,Codes,L> plusNum = 
      gb.seq(Op.PLUS, gb.token(Op.DEFAULT,Codes.PLUS)).add(product);

    Grammar<Expr,Codes,L> minusNum = 
      gb.seq(Op.MINUS, gb.token(Op.DEFAULT,Codes.MINUS)).add(product);
    
    Grammar<Expr,Codes,L> sum = 
      gb.seq(Op.SUM, product)
      .add(gb.star(gb.choice(plusNum).or(minusNum)));

    Grammar<Expr,Codes,L> parenthesized = 
      gb.seq(Op.PAREN, gb.token(Op.DEFAULT,Codes.OPAREN))
      .add(sum)
      .add(gb.token(Op.DEFAULT,Codes.CPAREN));

    recExpr.setChild(parenthesized);
    
    parser = sum.compile();
  }
  /* +***************************************************************** */
  private static enum Op
      implements NodeFactory<Expr,Codes>, LeafFactory<Expr,Codes,L> {

    DEFAULT, TIMES, DIVIDE, PLUS, MINUS, PRODUCT {
      public Expr create(List<Expr> children) {
        if( children.size()==1 ) return children.get(0);
        Expr e = children.remove(1);
        children.addAll(e.getChildren());
        return new Expr(this, children);
      }
    },
    SUM {
      public Expr create(List<Expr> children) {
        if( children.size()==1 ) return children.get(0);
        Expr e = children.remove(1);
        children.addAll(e.getChildren());
        return new Expr(this, children);
      }
    },
    NUMBER {
      @Override
      public Expr create(L lex)
      {
        TestToken t = lex.currentToken();
        return new Expr(Double.parseDouble(t.getText()));
      }
    },
    PAREN {
      public Expr create(List<Expr> children) {
        // children.remove(0);
        // children.remove(1);
        return children.get(0);
      }
    };

    public Expr create(List<Expr> children) {
      return new Expr(this, children);
    }

    public Expr create(L lex) {
      // TODO Auto-generated method stub
      return null;
    }
  }
  /* +***************************************************************** */
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
