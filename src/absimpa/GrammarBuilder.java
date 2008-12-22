package absimpa;


/**
 * <p>
 * helps to build a {@link Grammar} for a language made from objects of type
 * {@code C}, <b>start here</b>. To create your grammar, the following bits and pieces are
 * needed.
 * </p>
 * <ol>
 * <li>A {@link absimpa.Lexer} is needed that provides token codes of type
 * {@code C}. It is completely up to you what these token codes
 * stand for. For the parser and the grammar the only interesting bit is that
 * {@code C} is an {@link java.lang.Enum}. Typically a token code signifies
 * a type of token like NUMBER, OPENBRACKET, IDENTIFIER and so on. Normally
 * the lexer will internally associate the current token code also with a
 * piece of text, the token text, but the parser we built is never interested
 * in this text. Nevertheless, the text is not lost. Read on.</li>
 * <li>A result type {@code N} is needed. This package makes no assumption
 * whatsoever what the result type is. Whenever the parser has recognized a
 * partial bit of input, it will call either a {@link LeafFactory} or a
 * {@link NodeFactory} with the bits it has recognized and asks them to
 * create an {@code N}.</li>
 * <li>One or more {@link LeafFactory} objects are needed. They are
 * called by the parser whenever a token code provided by the lexer is
 * recognized. The parser will call {@link LeafFactory#create} with the lexer
 * as parameter to obtain an {@code N} for the token (code). Which factory is
 * called, depends on the parameter that is passed to {@link #token} when the
 * grammar is built.</li>
 * <li>One or more {@link NodeFactory} objects are needed. Whenever the
 * parser recognizes a part of the input, for example a sequence of tokens as
 * described by the {@link #seq seq()} method, it will call the associated
 * {@code NodeFactory} with the bits it recognized to obtain a new {@code N}.</li>
 * </ol>
 * 
 * <p>
 * To build a grammar, start by defining a few token recognizing grammars
 * with {@link #token token()}. Then you can combine these, for example into
 * a sequence, by passing the first to {@link #seq seq()} and then adding
 * more with {@link Sequence#add}. To repeat things, use
 * {@link #star star()} or {@link #repeat repeat()}. Use
 * {@link #choice choice()} and subsequently {@link Choice#or Choice.or()} to
 * create an choice of subnodes.
 * </p>
 * 
 * <p>
 * Slightly tricky is the creation of a recursive grammar. Create a
 * {@link Recurse} as a placeholder and later set its recursive child with
 * {@link Recurse#setChild(Grammar)}.
 * </p>
 * 
 * <p>
 * <b>Remark:</b> This package has no support to create a
 * {@link absimpa.Lexer}. This is all up to you.
 * </p>
 * 
 * @param <N> is the type of the objects created by the parser to be
 *        generated from the grammar constructed with a
 *        {@code GrammarBuilder}
 * @param <C> is the type of token codes provided by the lexer that will be
 *        used by parser
 * @param <L> is the type of {@link absimpa.Lexer} from which the parser
 *        obtains token information
 */
public class GrammarBuilder<N,C extends Enum<C>,L extends Lexer<C>> {
  private final NodeFactory<N> defaultFactory;
  /*+******************************************************************/
  public GrammarBuilder(NodeFactory<N> defaultFactory) {
    this.defaultFactory = defaultFactory;
  }
  /*+******************************************************************/
  public TokenGrammar<N,C,L> token(LeafFactory<N,C,L> fac, C code) {
    return new TokenGrammar<N,C,L>(fac, code);
  }
  /* +***************************************************************** */
  public Sequence<N,C,L> seq(NodeFactory<N> inner, Grammar<N,C,L> g) {
    return new Sequence<N,C,L>(inner, g);
  }
  public Sequence<N,C,L> seq(Grammar<N,C,L> g) {
    return new Sequence<N,C,L>(defaultFactory, g);
  }
  /*+******************************************************************/
  public Repeat<N,C,L> repeat(NodeFactory<N> inner, Grammar<N,C,L> grammar,
                            int min, int max)
  {
    return new Repeat<N,C,L>(inner, min, max, grammar);
  }
  public Repeat<N,C,L> repeat(Grammar<N,C,L> grammar, int min, int max) {
    return new Repeat<N,C,L>(defaultFactory, min, max, grammar);
  }
  public Repeat<N,C,L> star(Grammar<N,C,L> grammar) {
    return repeat(grammar, 0, Integer.MAX_VALUE);
  }
  public Repeat<N,C,L> star(NodeFactory<N> nf, Grammar<N,C,L> grammar) {
    return repeat(nf, grammar, 0, Integer.MAX_VALUE);
  }
  /*+******************************************************************/
  /*+******************************************************************/
  public Choice<N,C,L> choice(Grammar<N,C,L> grammar) {
    return new Choice<N,C,L>(grammar);
  }
  /*+******************************************************************/
}
