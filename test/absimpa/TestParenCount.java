package absimpa;

import org.junit.Before;

import absimpa.lexer.SimpleLexer;

public class TestParenCount {
  private enum Tokens {
    LPAREN, RPAREN, EOF;
  }
  private SimpleLexer<Integer, Tokens> l;
  
  @Before
  public void setup() {
    l = new SimpleLexer<>(Tokens.EOF, 
                          lex->lex.current()==Tokens.LPAREN ? -1 : 1);
    l.addToken(Tokens.LPAREN, "[(]");
    l.addToken(Tokens.RPAREN, "[)]");
    
    TokenGrammar<Integer, Tokens> lp = new TokenGrammar<>(Tokens.LPAREN);
    TokenGrammar<Integer, Tokens> rp = new TokenGrammar<>(Tokens.LPAREN);
    Recurse<Integer, Tokens> rec = new Recurse<>();
    Grammar<Integer, Tokens> optrec = rec.opt();
    Sequence<Integer, Tokens> pair = new Sequence<>(lp).add(optrec).add(rp);
    
  }
}
