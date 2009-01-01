package example;

import java.io.IOException;
import java.util.ArrayList;
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
  public static class Eparser implements Parser<Expr,Codes,L> {
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
    EOF, NUMBER, PLUSMINUS, MULDIV, OPAREN, CPAREN;
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
    private List<Expr> ops = new ArrayList<Expr>(2);
    ExprOper(Etype t) {
      super(t);
      ops.add(null);
      ops.add(null);
    }
    public void setOp(int which, Expr op) {
      ops.set(which, op);
    }
    public void addOp(Expr op) {
      ops.add(op);
    }
    public Expr getOp(int i) {
      return ops.get(i);
    }
    @Override
    public Number value() {
      double v0 = ops.get(0).value().doubleValue();
      double v1 = ops.get(1).value().doubleValue();
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
      for(Expr e : ops) {
        e.dump(app, "  "+indent);
      }
    }
  }
  /*+******************************************************************/
  public static L createLexer() {
    return new L(Codes.EOF)
    .addToken(Codes.PLUSMINUS, "[-+]")
    .addToken(Codes.NUMBER, "[1-9][0-9]*|0")
    .addToken(Codes.MULDIV, "[*/]")
    .addToken(Codes.OPAREN, "[(]")
    .addToken(Codes.CPAREN, "[)]")
    ;    
  }
  public static Eparser createParser() {
    // term -> NUMBER | expr
    // product -> term ( (mul | div ) term)*
    // sum -> product ( (add | sub ) product)*
    // expr -> '(' sum ')'
    GrammarBuilder<Expr,Codes,L> gb =
        new GrammarBuilder<Expr,Codes,L>(null, null);

    Recurse<Expr,Codes,L> recExpr = new Recurse<Expr,Codes,L>();

    Grammar<Expr,Codes,L> NUMBER = gb.token(Leafs.NUMBER, Codes.NUMBER);
    Grammar<Expr,Codes,L> SIGN = gb.token(Leafs.OPERATOR, Codes.PLUSMINUS);
    Grammar<Expr,Codes,L> MULDIV = gb.token(Leafs.OPERATOR, Codes.MULDIV);
    Grammar<Expr,Codes,L> OPAREN = gb.token(Leafs.IGNORE, Codes.OPAREN);
    Grammar<Expr,Codes,L> CPAREN = gb.token(Leafs.IGNORE, Codes.CPAREN);

    Grammar<Expr,Codes,L> signum = 
      gb.seq(Inner.SIGN, gb.opt(Inner.PICKFIRST, SIGN)).add(NUMBER);

    Grammar<Expr,Codes,L> term = gb.choice(signum).or(recExpr);

    Grammar<Expr,Codes,L> duct = gb.seq(Inner.APPLYRIGHT, MULDIV).add(term);

    Grammar<Expr,Codes,L> product =
        gb.seq(Inner.APPLYLEFT, term).add(gb.star(Inner.LIST,duct));

    Grammar<Expr,Codes,L> um = gb.seq(Inner.APPLYRIGHT, SIGN).add(product);

    Grammar<Expr,Codes,L> sum =
        gb.seq(Inner.APPLYLEFT, product).add(gb.star(Inner.LIST, um));

    Grammar<Expr,Codes,L> parenthesized =
        gb.seq(Inner.PICKFIRST, OPAREN).add(sum).add(CPAREN);

    recExpr.setChild(parenthesized);

    return new Eparser(sum.compile());
  }
  /* +***************************************************************** */
  private static enum Leafs implements LeafFactory<Expr,Codes,L> {
    NUMBER {
      public Expr create(L lex) {
        String t = lex.currentText();
        Double d = Double.parseDouble(t);
        return new ExprNum(d);
      }
    },
    OPERATOR {
      public Expr create(L lex) {
        String t = lex.currentText();
        int count = 0;
        for(int i=0; i<t.length(); i++) {
          if( t.charAt(i)=='-') count+=1;
        }
        if( count%2==1 ) {
          return new ExprOper(Etype.MINUS);
        }
        char ch = t.charAt(0);
        if( ch=='*') return new ExprOper(Etype.TIMES);
        if( ch=='/') return new ExprOper(Etype.DIVIDE);
        return new ExprOper(Etype.PLUS);
      }
    },
    IGNORE {
      public Expr create(L lex) {
        return null;
      }
    }
    ;  
  }
  /* +***************************************************************** */
  private static enum Inner implements NodeFactory<Expr> {
    APPLYRIGHT {
      public Expr create(List<Expr> children) {
        ExprOper op = (ExprOper)(children.get(0));
        Expr e = children.get(1);
        op.setOp(1, e);
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
          tmp = (ExprOper)op.getOp(0);
        }
        op.setOp(0, children.get(0));
        return children.get(1);
      }      
    },
    LIST {
      public Expr create(List<Expr> children) {
        if( children.size()==0 ) return null;
        int l = children.size();
        ExprOper last = (ExprOper)children.get(l-1);
        for(int i=l-2; i>=0; i--) {
          ExprOper prev = (ExprOper)children.get(i);
          last.setOp(0, prev);
          last = prev;
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
        } else {
          return num;
        }
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
