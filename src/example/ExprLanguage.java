package example;

import java.io.IOException;
import java.util.List;

import absimpa.*;

/**
 * create a simple expression language and use it for testing.
 *
 */
public class ExprLanguage {
  public static class L extends TrivialLexer<Codes> {
    public L(Codes eofCode) {
      super(eofCode);
    }    
    @Override
    public L addToken(Codes tc, String regex) {
      super.addToken(tc, regex);
      return this;
    }
  }
  public static class Eparser implements ParserI<Expr,Codes,L> {
    private final Parser<Expr,Codes,L> p;
    public Eparser(Parser<Expr,Codes,L> p) {
      this.p = p;
    }
    @Override
    public Expr parse(L lex) throws ParseException
    {
      return p.parse(lex);
    }
  }
  private static enum Codes {
    EOF, NUMBER, PLUS, MINUS, TIMES, DIVIDE, OPAREN, CPAREN;
  }
  /*+******************************************************************/
  public static final class Expr {
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
  public static L createLexer() {
    return new L(Codes.EOF)
    .addToken(Codes.NUMBER, "[+-]?[1-9][0-9]*|0")
    .addToken(Codes.PLUS, "[+]")
    .addToken(Codes.MINUS, "-")
    .addToken(Codes.TIMES, "[*]")
    .addToken(Codes.DIVIDE, "[/]")
    .addToken(Codes.OPAREN, "[(]")
    .addToken(Codes.CPAREN, "[)]")
    ;    
  }
  public static Eparser createParser() {
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
    
    return new Eparser(sum.compile());
  }
  /* +***************************************************************** */
  private static enum Op
      implements NodeFactory<Expr>, LeafFactory<Expr,Codes,L> {

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
        Token t = lex.currentToken();
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
}
