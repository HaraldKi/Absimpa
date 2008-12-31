package absimpa;

import java.util.*;


/**
 * <p>
 * A recursive decent parser which transforms information provided by a
 * {@link absimpa.Lexer} into some type {@code N}.
 * </p>
 * <p>
 * To obtain a {@code Parser}, you need to create a {@link Grammar}, for
 * example by using the {@link GrammarBuilder} and then call
 * {@link Grammar#compile()}.
 * </p>
 * 
 * @param <N> is the type of the objects created by the parser
 * @param <C> is the type of token codes provided by the lexer
 * @param <L> is the type of {@link absimpa.Lexer} from which the parser
 *        obtains tokens information
 */
public abstract class Parser<N,C extends Enum<C>,L extends Lexer<C>>
    implements ParserI<N,C,L>
{
  private Parser() { }

   public abstract N parse(L lex) throws ParseException;
  /* +***************************************************************** */
  static class TokenParser<N,C extends Enum<C>,L extends Lexer<C>>
      extends Parser<N,C,L>
  {
    private final LeafFactory<N,C,L> leafFactory;
    private final C tokenCode;
    TokenParser(C tokenCode, LeafFactory<N,C,L> lf) {
      this.leafFactory = lf;
      this.tokenCode = tokenCode;
    }
    public N parse(L lex) throws ParseException {
      C code = lex.current();
      if( code!=tokenCode ) throw lex.parseException(EnumSet.of(tokenCode));

      // N node = leafFactory.create(lex);
      N node = leafFactory.create(lex);
      lex.next();
      return node;
    }
    public String toString() {
      return String.format("%s->%s", tokenCode, leafFactory);
    }
  }
  /* +***************************************************************** */
  static class ChoiceParser<N,C extends Enum<C>,L extends Lexer<C>>
      extends Parser<N,C,L>
  {
    private final boolean optional;
    private final EnumMap<C,Parser<N,C,L>> choiceMap;

    ChoiceParser(boolean optional, EnumMap<C,Parser<N,C,L>> choiceMap) {
      this.optional = optional;
      this.choiceMap = choiceMap;
    }
    public N parse(L lex) throws ParseException {
      C code = lex.current();
      Parser<N,C,L> p = choiceMap.get(code);
      if( p!=null ) return p.parse(lex);
      if( optional ) return null;
      throw lex.parseException(choiceMap.keySet());
    }
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("CHOICE{");
      String sep = "";
      for(C c : choiceMap.keySet() ) {
        sb.append(sep).append(c);
        sep = ",";
      }
      sb.append('}');
      return sb.toString();
    }
  }
  /* +***************************************************************** */
  static class SeqParser<N,C extends Enum<C>,L extends Lexer<C>>
      extends Parser<N,C,L>
  {
    private final List<Parser<N,C,L>> children;
    private final NodeFactory<N> nf;
    SeqParser(NodeFactory<N> nf, List<Parser<N,C,L>> children) {
      this.nf = nf;
      this.children = children;
    }
    public N parse(L lex) throws ParseException {
      List<N> nodes = new ArrayList<N>(children.size());
      for(Parser<N,C,L> p : children) {
        N child = p.parse(lex);
        if( child!=null ) nodes.add(child);
      }
      return nf.create(nodes);
    }
    public String toString() {
      return String.format("SEQ(%s)", nf);
    }
  }
  /* +***************************************************************** */
  static class RepeatParser<N,C extends Enum<C>,L extends Lexer<C>>
      extends Parser<N,C,L>
  {
    private final int min, max;
    private final EnumSet<C> childLookahead;
    private Parser<N,C,L> child;
    private final NodeFactory<N> nf;

    RepeatParser(EnumSet<C> childLookahead, NodeFactory<N> nf,
                 Parser<N,C,L> child, int min, int max) {
      if( min<0||max<min||max==0 ) {
        String msg =
            String.format("must have 0<=min<=max and max>0, but have "
                +" min=%d, max=%d", min, max);
        throw new IllegalArgumentException(msg);
      }
      this.nf = nf;
      this.min = min;
      this.max = max;
      this.childLookahead = childLookahead;
      this.child = child;
    }

    public N parse(L lex) throws ParseException {
      List<N> nodes = new ArrayList<N>(min);

      int count = 0;
      C code = lex.current();
      while( count<min||(count<max&&childLookahead.contains(code)) ) {
        // REMINDER: child may be an optional node that continuously
        // returns null due to a lookahead mismatch. In that case we loop
        // until min without progress in reading input. Thats ok, because
        // most of the time min is either 0 or 1, or the child is not
        // optional.
        N node = child.parse(lex);
        if( node!=null ) nodes.add(node);
        count += 1;
        code = lex.current();
      }
      return nf.create(nodes);
    }
    public String toString() {
      return String.format("REP(%s){%d,%d, la=%s}", nf, min, max, childLookahead);
    }
  }
  /* +***************************************************************** */
  static class RecurseParser<N,C extends Enum<C>,L extends Lexer<C>>
      extends Parser<N,C,L>
  {
    private Parser<N,C,L> child;
    void setChild(Parser<N,C,L> child) {
      this.child = child;
    }
    public N parse(L lex) throws ParseException {
      return child.parse(lex);
    }
    public String toString() {
      return "RECURSE";
    }
  }
  /* +***************************************************************** */
}
