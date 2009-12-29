package absimpa;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import example.LeafFactory;
import example.TrivialLexer;

public class TestPicky {
  private L lex = null;
  GrammarBuilder<String,Codes> gb;
  Grammar<String,Codes> term, number, space, ignore, plus;
  /* +***************************************************************** */
  private static enum Codes
      implements LeafFactory<String,Codes> {
    TERM, NUMBER, SPACE, IGNORE, PLUS, EOF;

    @Override
    public String create(TrivialLexer<String,Codes> lex) {
      if( this==Codes.SPACE ) return null;
      if( this==Codes.IGNORE ) return null;
      return "("+lex.currentToken().getText()+")";
    }
  }
  /*+******************************************************************/
  private static final class L extends TrivialLexer<String,Codes> {
    public L(Codes eofToken) {
      super(eofToken);
      addToken(Codes.TERM, "[A-Za-z][A-Za-z0-9]*");
      addToken(Codes.NUMBER, "[0-9]+");
      addToken(Codes.SPACE, "\\s+");
      addToken(Codes.IGNORE, "[!]+");
      addToken(Codes.PLUS, "[+]");
    }
  }
  /* +***************************************************************** */
  private static final class NodeMaker
      implements NodeFactory<String>
  {
    private final String name;
    public NodeMaker(String name) {
      this.name = name;
    }
    @Override
    public String create(List<String> children) {
      StringBuilder sb = new StringBuilder();
      sb.append(name).append("[");
      String sep = "";
      for(String child : children) {
        sb.append(sep).append(child);
        sep = ",";
      }
      sb.append("]");
      return sb.toString();
    }
  }
  /*+******************************************************************/
  @Before
  public void startup() {
    lex = new L(Codes.EOF);
    gb = new GrammarBuilder<String,Codes>(null);
    term = gb.token(Codes.TERM);
    number = gb.token(Codes.NUMBER);
    space = gb.token(Codes.SPACE);
    ignore = gb.token(Codes.IGNORE);
    plus = gb.token(Codes.PLUS);
  }
  /* +***************************************************************** */
  private String analyze(String text, Parser<String,Codes> p)
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
    Grammar<String,Codes> star = gb.star(new NodeMaker("star"), term);
    Grammar<String,Codes> terms = 
      gb.repeat(new NodeMaker("rep"), 
                gb.choice(number).or(space).or(ignore), 1, 888888);
    
    Grammar<String,Codes> g =
        gb.seq(new NodeMaker("top"), star)
        .add(terms);

    Parser<String,Codes> p = g.compile();

    String result = analyze("   123 456 789", p);
    //System.out.printf("%s%n", result);
    assertEquals("top[rep[(123),(456),(789)]]", result);

    result = analyze("1234", p);
    //System.out.printf("%s%n", result);    
    assertEquals("top[rep[(1234)]]", result);
    
    result = analyze("  !!  !!", p);
    //System.out.printf("%s%n", result);
    assertEquals("top[rep[]]", result);
  }
  /*+******************************************************************/
  @Test(expected=LookaheadConflictException.class)
  public void shouldNeverHappenInLAConflictException() throws Exception {
    Grammar<String,Codes> optIgnore = 
      gb.opt(new NodeMaker("opt"), ignore);
    Grammar<String,Codes> seq = 
      gb.seq(new NodeMaker("seq"), optIgnore).add(optIgnore);
    seq.compile();
  }
  /*+******************************************************************/
  @Test
  public void seqNotCreateEpsilon() throws Exception {
    Grammar<String,Codes> optIgnore = 
      gb.opt(new NodeMaker("opt"), ignore);
    Grammar<String,Codes> optSpace= 
      gb.opt(new NodeMaker("opt"), space);
    Grammar<String,Codes> seq = 
      gb.seq(new NodeMaker("seq"), optIgnore).add(optSpace);
    Parser<String,Codes> p = seq.compile();

    String result = analyze("!!  abc", p);
    //System.out.printf("%s%n", result);
    assertEquals("seq[opt[],opt[]]", result);
    
    Grammar<String,Codes> seqseq = 
      gb.seq(new NodeMaker("seqseq"), seq).add(term);
    p = seqseq.compile();
    result = analyze("abc", p);
    //System.out.printf("%s%n", result);
    assertEquals("seqseq[(abc)]", result);
    
    result = analyze("!!abc", p);
    //System.out.printf("%s%n", result);
    assertEquals("seqseq[seq[opt[]],(abc)]", result);
  }
  /*+******************************************************************/
  @Test
  public void orOverAllEpsilons() throws Exception {
    Grammar<String,Codes> optTerm = gb.opt(term);
    Grammar<String,Codes> optNumber = gb.opt(number);
    
    Grammar<String,Codes> g = 
      gb.seq(new NodeMaker("top"),gb.choice(optTerm).or(optNumber)).add(plus);
    
    Parser<String,Codes> p = g.compile();
    String result = analyze("+", p);
    // System.out.println(result);
    assertEquals("top[(+)]", result);
  }

}
