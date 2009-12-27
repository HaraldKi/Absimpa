package absimpa;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import example.TrivialLexer;

public class TestPicky {
  private L lex = null;
  GrammarBuilder<String,Codes,L> gb;
  Grammar<String,Codes,L> term, number, space, ignore;
  
  private static enum Codes {
    TERM, NUMBER, SPACE, IGNORE, EOF;
  }
  private static final class L extends TrivialLexer<Codes> {
    public L(Codes eofToken) {
      super(eofToken);
      addToken(Codes.TERM, "[A-Za-z][A-Za-z0-9]*");
      addToken(Codes.NUMBER, "[0-9]+");
      addToken(Codes.SPACE, "\\s+");
      addToken(Codes.IGNORE, "[!]+");
    }
  }
  private static final class NodeMaker
      implements NodeFactory<String>, LeafFactory<String,Codes,L>
  {
    private final String name;
    public NodeMaker(String name) {
      this.name = name;
    }
    @Override
    public String create(List<String> children) {
      StringBuilder sb = new StringBuilder();
      sb.append(name).append("[");
      String sep="";
      for(String child : children) {
        sb.append(sep).append(child);
        sep = ",";
      }
      sb.append("]");
      return sb.toString();
    }

    @Override
    public String create(L lex) {
      if( lex.current()==Codes.SPACE ) return null;
      if( lex.current()==Codes.IGNORE) return null;
      return "("+lex.currentToken().getText()+")";
    }
  }
  /*+******************************************************************/
  @Before
  public void startup() {
    lex = new L(Codes.EOF);
    gb =
      new GrammarBuilder<String,Codes,L>(null, new NodeMaker("token"));
    term = gb.token(Codes.TERM);
    number = gb.token(Codes.NUMBER);
    space = gb.token(Codes.SPACE);
    ignore = gb.token(Codes.IGNORE);
  }
  /* +***************************************************************** */
  private String analyze(String text, Parser<String,Codes,L> p)
    throws Exception
  {
    lex.initAnalysis(text);
    return p.parse(lex);
  }
  /*+******************************************************************/
  /**
   * in response to a bug where repeat(...,0,...) would call the node factory
   * for an element that reduced to epsilon.
   */
  @Test
  public void repeatNotCreateEpsilon() throws Exception {
    Grammar<String,Codes,L> star = gb.star(new NodeMaker("star"), term);
    Grammar<String,Codes,L> terms = 
      gb.repeat(new NodeMaker("rep"), 
                gb.choice(number).or(space).or(ignore), 1, 888888);
    
    Grammar<String,Codes,L> g =
        gb.seq(new NodeMaker("top"), star)
        .add(terms);

    Parser<String,Codes,L> p = g.compile();

    String result = analyze("   123 456 789", p);
    System.out.printf("%s%n", result);
    assertEquals("top[rep[(123),(456),(789)]]", result);

    result = analyze("1234", p);
    System.out.printf("%s%n", result);    
    assertEquals("top[rep[(1234)]]", result);
    
    result = analyze("  !!  !!", p);
    System.out.printf("%s%n", result);
    assertEquals("top[rep[]]", result);
  }
  /*+******************************************************************/
  @Test(expected=LookaheadConflictException.class)
  public void shouldNeverHappenInLAConflictException() throws Exception {
    Grammar<String,Codes,L> optIgnore = 
      gb.opt(new NodeMaker("opt"), ignore);
    Grammar<String,Codes,L> seq = 
      gb.seq(new NodeMaker("seq"), optIgnore).add(optIgnore);
    seq.compile();
  }
  /*+******************************************************************/
  @Test
  public void seqNotCreateEpsilon() throws Exception {
    Grammar<String,Codes,L> optIgnore = 
      gb.opt(new NodeMaker("opt"), ignore);
    Grammar<String,Codes,L> optSpace= 
      gb.opt(new NodeMaker("opt"), space);
    Grammar<String,Codes,L> seq = 
      gb.seq(new NodeMaker("seq"), optIgnore).add(optSpace);
    Parser<String,Codes,L> p = seq.compile();
    String result = analyze("!!  abc", p);
    System.out.printf("%s%n", result);
    assertEquals("seq[opt[],opt[]]", result);
    
    Grammar<String,Codes,L> seqseq = 
      gb.seq(new NodeMaker("seqseq"), seq).add(term);
    p = seqseq.compile();
    result = analyze("abc", p);
    System.out.printf("%s%n", result);
    assertEquals("seq[(abc)]", result);
  }
}
