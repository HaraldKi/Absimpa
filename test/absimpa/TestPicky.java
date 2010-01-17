package absimpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import absimpa.bnf.SimpleLexer;

import example.LeafFactory;

public class TestPicky {
  private L lex = null;
  GrammarBuilder<String,Codes> gb;
  Grammar<String,Codes> term, number, space, ignore, plus, minus;
  /* +***************************************************************** */
  private static enum Codes
      implements LeafFactory<String,Codes> {
    TERM, NUMBER, SPACE, IGNORE, PLUS, MINUS, EOF;

    @Override
    public String create(SimpleLexer<String,Codes> lex) {
      if( this==Codes.SPACE ) return null;
      if( this==Codes.IGNORE ) return null;
      return "("+lex.currentToken().getText()+")";
    }
  }
  /*+******************************************************************/
  private static final class L extends SimpleLexer<String,Codes> {
    public L(Codes eofToken) {
      super(eofToken, leafFactory);
      addToken(Codes.TERM, "[A-Za-z][A-Za-z0-9]*");
      addToken(Codes.NUMBER, "[0-9]+");
      addToken(Codes.SPACE, "\\s+");
      addToken(Codes.IGNORE, "[!]+");
      addToken(Codes.PLUS, "[+]");
      addToken(Codes.MINUS, "[-]");
    }
  }
  /*+******************************************************************/
  private static LeafFactory<String,Codes> leafFactory = new LeafFactory<String,Codes>() {
    @Override
    public String create(SimpleLexer<String,Codes> lex)
      throws ParseException
    {
      return lex.current().create(lex);
    }
    
  };
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
    minus = gb.token(Codes.MINUS);
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
  @Test
  public void seqNotCreateEpsilon() throws Exception {
    Grammar<String,Codes> optIgnore = 
      gb.opt(new NodeMaker("opt"), ignore);
    Grammar<String,Codes> optSpace= 
      gb.opt(new NodeMaker("opt"), space);
    Grammar<String,Codes> seq = 
      gb.seq(optIgnore, optSpace).setNodeFactory(new NodeMaker("seq"));
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
  /*+******************************************************************/
  @Test
  public void laConflictInOr() throws Exception {
    Grammar<String,Codes> optTermMinus = 
      gb.opt(gb.seq(term, minus).setNodeFactory(new NodeMaker("seqm")));
    Grammar<String,Codes> optTermPlus = 
      gb.opt(gb.seq(term, plus).setNodeFactory(new NodeMaker("seqp")));
    Grammar<String,Codes> choice = 
      gb.choice(optTermMinus, optTermPlus);
    
    // show that bla- is parse because the minus arm of choice has preceedence
    Parser<String,Codes> p = choice.compile();
    String result = analyze("bla-", p);
    //System.out.println(result);
    assertEquals("seqm[(bla),(-)]", result);

    // now show tht bla+ is a parse error for the same reason
    Exception e = new Exception();
    try {
      analyze("bla+", p);
    } catch(ParseException ee) {
      e = ee;
    }
    //System.out.println(e.getMessage());
    assertTrue(e.getMessage().startsWith("1:4:found token `PLUS(+)' "));
  }
  /*+******************************************************************/
  @Test
  public void autoFlattening() throws Exception {
    Grammar<String,Codes> g =
      gb.seq(gb.opt(term),
             plus,
             gb.opt(number),
             minus,
             term).setNodeFactory(new NodeMaker("top"));
    Parser<String,Codes> p = g.compile();
    String result = analyze("abc+123-xyz", p);
    //System.out.println(result);
    assertEquals("top[(abc),(+),(123),(-),(xyz)]", result);
    
    result = analyze("+-abc", p);
    //System.out.println(result);
    assertEquals("top[(+),(-),(abc)]", result);
  }
  /*+******************************************************************/
  @Test
  public void anyGrammarWithNodeFactory() throws Exception {
    Grammar<String,Codes> token = 
      gb.token(Codes.TERM).setNodeFactory(new NodeMaker("toktok"));

    Parser<String,Codes> p = token.compile();
    String result = analyze("hallo", p);
    assertEquals("toktok[(hallo)]", result);
    
    Grammar<String,Codes> choice = 
      gb.choice(token).setNodeFactory(new NodeMaker("choice"));
    p = choice.compile();
    result = analyze("blabla", p);
    assertEquals("choice[toktok[(blabla)]]", result);
  }
  /*+******************************************************************/
  @Test(expected=IllegalStateException.class)
  public void missingToplevelNodeFactory() throws Exception {
    Grammar<String,Codes> g = gb.seq(term, space, number);
    Parser<String,Codes> p = g.compile();
    analyze("abc 123", p);
  }
  /*+******************************************************************/
  @Test
  public void starAndOpt() throws Exception {
    Grammar<String,Codes> g = 
      gb.seq(term.opt(new NodeMaker("oterm")),
             gb.seq(space,number).star(new NodeMaker("mystar"))
      ).setNodeFactory(new NodeMaker("top"));
    
    Parser<String,Codes> p = g.compile();
    String result = analyze("abc 123 45 234 234", p);
    System.out.println(result);
    assertEquals("top[oterm[(abc)],mystar[(123),(45),(234),(234)]]", result);
     
  }
  /*+******************************************************************/
}
