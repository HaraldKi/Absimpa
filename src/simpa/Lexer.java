package simpa;

import java.io.IOException;

import monq.jfa.AbstractFaAction;
import monq.jfa.CharSequenceCharSource;
import monq.jfa.CompileDfaException;
import monq.jfa.Dfa;
import monq.jfa.DfaRun;
import monq.jfa.FaAction;
import monq.jfa.Nfa;
import monq.jfa.ReSyntaxException;
import static simpa.Token.Code;
/**
 * <p>
 * trivial and slow implementation of a lexer for the simple query grammar.
 * </p>
 * 
 */
public class Lexer implements lexer.Lexer {
  private static final String TERMRE = "([A-Za-z0-9]+)";
  private static final String PHRASERE = "\"[^\"]*\"";
  private static final String SCOPERE = "[A-Za-z0-9]+[\r\n\t ]*:";
  private static final Dfa dfa;
  
  private final StringBuffer tokenBuffer = new StringBuffer(100);
  private final DfaRun run;
  private Token currentToken = null;
  
  /*+******************************************************************/
  private static final class TokenAction extends AbstractFaAction {
    private final Code code;
    public TokenAction(Code code) {
      this.code = code;
    }
    public void invoke(StringBuffer buf, int start, DfaRun run) {
      //Lexer lex = (Lexer)run.clientData;
    }    
    public Code getTokencode() {
      return code;
    }
  }
  /* +***************************************************************** */
  static {
    Nfa nfa;
    try {
      nfa = new Nfa(Nfa.NOTHING)
      .or(TERMRE, new TokenAction(Code.TERM))
      .or(PHRASERE, new TokenAction(Code.PHRASE))
      .or(SCOPERE, new TokenAction(Code.SCOPE))
      .or("OR", new TokenAction(Code.OR).setPriority(1))
      .or("NOT|-", new TokenAction(Code.NOT).setPriority(1))
      ;
    } catch( ReSyntaxException e1 ) {
      throw new RuntimeException("not possible", e1);
    }
    try {
      dfa = nfa.compile(DfaRun.UNMATCHED_DROP);
    } catch( CompileDfaException e ) {
      throw new RuntimeException("not possible", e);
    }
  }
  /*+******************************************************************/
  public Lexer(String text) {
    run = new DfaRun(dfa, new CharSequenceCharSource(text));
    next();
  }
  /*+******************************************************************/
  public Token next() {
    TokenAction ta = null;
    try {
      FaAction a = run.next(tokenBuffer);
      if( a!=null && a!=DfaRun.EOF ) ta = (TokenAction)a;
    } catch( IOException e ) {
      throw new RuntimeException("this can never happen");
    }
    if( ta==null ) {
      currentToken = new Token(Token.Code.EOF, "");
    } else {
      String tokenText = tokenBuffer.toString();
      tokenBuffer.setLength(0);
      currentToken = new Token(ta.getTokencode(), tokenText);
    }
    System.out.printf("delivering %s%n", currentToken);
    return currentToken;
  }
  /*+******************************************************************/
  public Token currentToken() {
    return currentToken;
  }
  /*+******************************************************************/
  public static void main(String[] argv) {
    StringBuilder sb = new StringBuilder();
    for(String a : argv) {
      sb.append(a).append(' ');
    }
    Lexer lex = new Lexer(sb.toString());
    for(Token t=lex.next(); t.code!=Code.EOF; t=lex.next()) {
      System.out.printf("%s%n", t);
    }
  }
}
 