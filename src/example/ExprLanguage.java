package example;

import java.io.IOException;
import java.util.List;

import absimpa.Grammar;
import absimpa.GrammarBuilder;
import absimpa.Lexer;
import absimpa.NodeFactory;
import absimpa.ParseException;
import absimpa.Parser;
import absimpa.Recurse;
import absimpa.lexer.LeafFactory;
import absimpa.lexer.SimpleLexer;

/**
 * <p>
 * create a simple expression language and use it for testing.
 * </p>
 *
 * <p>
 * See the <a href="../src-html/example/ExprLanguage.html">source code</a> for
 * the details.
 * </p>
 */
public class ExprLanguage {
  public static class Eparser implements Parser<Expr,Codes> {
    private final Parser<Expr,Codes> p;
    public Eparser(Parser<Expr,Codes> p) {
      this.p = p;
    }
    @Override
    public Expr parse(Lexer<Expr,Codes> lex) throws ParseException
    {
      return p.parse(lex);
    }
  }
  public static enum Codes implements LeafFactory<Expr,Codes> {
    PLUS {
      @Override
      public Expr create(SimpleLexer<Expr,Codes> lex) {
        return new ExprOper(Etype.PLUS);
      }
    },
    MINUS {
      @Override
      public Expr create(SimpleLexer<Expr,Codes> lex) {
        return new ExprOper(Etype.MINUS);
      }
    },
    TIMES {
      @Override
      public Expr create(SimpleLexer<Expr,Codes> lex) {
        return new ExprOper(Etype.TIMES);
      }
    },
    DIVIDE {
      @Override
      public Expr create(SimpleLexer<Expr,Codes> lex) {
        return new ExprOper(Etype.DIVIDE);
      }
    },
    NUMBER {
      @Override
      public Expr create(SimpleLexer<Expr,Codes> lex) {
        String t = lex.currentText();
        Double d = Double.parseDouble(t);
        return new ExprNum(d);
      }
    },
    OPAREN, CPAREN, EOF;

    @Override
    public Expr create(SimpleLexer<Expr,Codes> lex) {
      return null;
    }
  }
  private static class AutoMappedLeafFactory implements LeafFactory<Expr,Codes> {

    @Override
    public Expr create(SimpleLexer<Expr,Codes> lex) {
      return lex.current().create(lex);
    }
    
  }
  private static enum Etype {
    NUMBER, PLUS, MINUS, TIMES, DIVIDE;
  }
  /*+******************************************************************/
  public static abstract class Expr {
    public final Etype etype;
    Expr(Etype etype) {
      this.etype = etype;
    }
    public abstract Number value();
    public abstract void dump(Appendable app, String indent) throws IOException;
  }
  private static class ExprNum extends Expr {
    private final Number n;
    ExprNum(Number n) {
      super(Etype.NUMBER);
      this.n = n;
    }
    @Override
    public Number value() {
      return n;
    }
    public String toString() {
      return n.toString();
    }
    @Override
    public void dump(Appendable app, String indent) throws IOException {
      app.append(indent).append(toString()).append("\n");
    }
  }
  private static class ExprOper extends Expr {
    //private List<Expr> ops = new ArrayList<Expr>(2);
    Expr leftOp = null;
    Expr rightOp = null;
    ExprOper(Etype t) {
      super(t);
    }
    public void setLeftOp(Expr op) {
      leftOp = op;
    }
    public void setRightOp(Expr op) {
      rightOp = op;
    }
    public Expr getLeftOp() {
      return leftOp;
    }
    @Override
    public Number value() {
      double v0 = leftOp.value().doubleValue();
      double v1 = rightOp.value().doubleValue();
      switch(etype) {
      case PLUS: v0 = v0+v1; break;
      case MINUS: v0 = v0-v1; break;
      case TIMES: v0 = v0*v1; break;
      case DIVIDE: v0 = v0/v1; break;
      default: throw new IllegalStateException("cannot happen");
      }
      return new Double(v0);
    }
    public String toString() {
      return etype.toString();
    }
    @Override
    public void dump(Appendable app, String indent) throws IOException {
      app.append(indent).append(toString()).append("\n");
      leftOp.dump(app, "  "+indent);
      rightOp.dump(app, "  "+indent);
    }
  }
  /*+******************************************************************/
  public static SimpleLexer<Expr,Codes> createLexer() {
    return new SimpleLexer<>(Codes.EOF, new AutoMappedLeafFactory())
    .addToken(Codes.PLUS, "[+]")
    .addToken(Codes.MINUS, "[-]")
    .addToken(Codes.NUMBER, "[1-9][0-9]*|0")
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
    GrammarBuilder<Expr,Codes> gb = new GrammarBuilder<>(null);

    Recurse<Expr,Codes> recExpr = new Recurse<>();

    Grammar<Expr,Codes> NUMBER = gb.token(Codes.NUMBER);
    Grammar<Expr,Codes> SIGN =
        gb.choice(gb.token(Codes.PLUS)).or(gb.token(Codes.MINUS));
    Grammar<Expr,Codes> MULDIV =
        gb.choice(gb.token(Codes.TIMES)).or(gb.token(Codes.DIVIDE));
    Grammar<Expr,Codes> OPAREN = gb.token(Codes.OPAREN);
    Grammar<Expr,Codes> CPAREN = gb.token(Codes.CPAREN);

    Grammar<Expr,Codes> signum =
        gb.seq(Inner.SIGN, gb.opt(Inner.PICKFIRST, SIGN)).add(NUMBER)
          .setName("Signum");

    Grammar<Expr,Codes> term = gb.choice(signum).or(recExpr).setName("Term");

    Grammar<Expr,Codes> duct =
        gb.seq(Inner.APPLYRIGHT, MULDIV).add(term).setName("Duct");

    Grammar<Expr,Codes> product =
        gb.seq(Inner.APPLYLEFT, term).add(gb.star(Inner.LIST, duct))
          .setName("Product");

    Grammar<Expr,Codes> um =
        gb.seq(Inner.APPLYRIGHT, SIGN).add(product).setName("Um");

    Grammar<Expr,Codes> sum =
        gb.seq(Inner.APPLYLEFT, product).add(gb.star(Inner.LIST, um))
          .setName("Sum");

    Grammar<Expr,Codes> parenthesized =
        gb.seq(Inner.PICKFIRST, OPAREN).add(sum).add(CPAREN).setName("Expr");

    recExpr.setChild(parenthesized);
    //System.out.println(parenthesized.toBNF());

    return new Eparser(sum.compile());
  }
  /* +***************************************************************** */
  private static enum Inner implements NodeFactory<Expr> {
    APPLYRIGHT {
      public Expr create(List<Expr> children) {
        ExprOper op = (ExprOper)(children.get(0));
        Expr e = children.get(1);
        op.setRightOp(e);
        return op;
      }
    },
    APPLYLEFT {
      public Expr create(List<Expr> children) {
        if( children.size()==1 ) return children.get(0);
        ExprOper op = null;
        ExprOper tmp = (ExprOper)children.get(1);
        while( tmp!=null ) {
          op = tmp;
          tmp = (ExprOper)op.getLeftOp();
        }
        op.setLeftOp(children.get(0));
        return children.get(1);
      }      
    },
    LIST {
      public Expr create(List<Expr> children) {
        if( children.size()==0 ) return null;
        int l = children.size();
        ExprOper current = (ExprOper)children.get(l-1);
        for(int i=l-2; i>=0; i--) {
          ExprOper prev = (ExprOper)children.get(i);
          current.setLeftOp(prev);
          current = prev;
        }
        return children.get(l-1);
      }
    },
    SIGN {
      public Expr create(List<Expr> children) {
        if( children.size()==1 ) {
          return children.get(0);
        }
        ExprOper sign = (ExprOper)children.get(0);
        ExprNum num = (ExprNum)children.get(1);
        if( sign.etype==Etype.MINUS ) {
          return new ExprNum(-num.value().doubleValue());
        }
        return num;
      }
    },
    PICKFIRST {
      public Expr create(List<Expr> children) {
        if( children.size()==0 ) return null;
        return children.get(0);
      }      
    };
  }
  /* +***************************************************************** */
}
