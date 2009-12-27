package absimpa;

/**
 * <p>
 * helps to build a {@link Grammar} for a language made from objects of type
 * {@code C}. To create your grammar, the following bits and pieces are
 * needed.
 * </p>
 * <ol>
 * <li>A {@link absimpa.Lexer} is needed that provides token codes of type
 * {@code C}. It is completely up to you what these token codes stand for.
 * For the parser and the grammar the only interesting bit is that {@code C}
 * is an {@link java.lang.Enum}. Typically a token code signifies a type of
 * token like NUMBER, OPENBRACKET, IDENTIFIER and so on. Normally the lexer
 * will internally associate the current token code also with a piece of
 * text, the token text, but the parser we built is never interested in this
 * text. Nevertheless, the text is not lost. Read on.</li>
 * 
 * <li>A result type {@code N} is needed. This package makes no assumption
 * about the type of result created, it is just called {@code N}. Whenever
 * the parser has recognized a partial bit of input, it will call either a
 * {@link LeafFactory} or a {@link NodeFactory} with the bits it has
 * recognized and asks them to create an {@code N}.</li>
 * 
 * <li>One or more {@link LeafFactory} objects are needed. They are called by
 * the parser whenever a token code provided by the lexer is recognized. The
 * parser will call {@link LeafFactory#create} with the lexer as parameter to
 * obtain an {@code N} for the token (code). Which factory is called, depends
 * on the parameter that is passed to {@link #token} when the grammar is
 * built.</li>
 * <li>One or more {@link NodeFactory} objects are needed. Whenever the
 * parser recognizes a part of the input, for example a sequence of tokens as
 * described by the {@link #seq seq()} method, it will call the associated
 * {@code NodeFactory} with the bits it recognized to obtain a new {@code N}.
 * </li>
 * </ol>
 * 
 * <p>
 * It is up to you to define what {@code N} is. It may be a type that
 * describes a syntax tree, but it may as well describe something completely
 * different that is incrementally built up while parsing.
 * </p>
 * 
 * <p>
 * To build a grammar, start by defining a few token recognizing grammars
 * with {@link #token token()}. Then you can combine these, for example into
 * a sequence, by passing the first to {@link #seq seq()} and then adding
 * more with {@link Sequence#add}. To repeat things, use {@link #star star()}
 * or {@link #repeat repeat()}. Use {@link #choice choice()} and subsequently
 * {@link Choice#or Choice.or()} to create an choice of subnodes. A rough
 * outline of an example is:
 * </p>
 * 
 * <pre>
 * GrammarBuilder&lt;...&gt; gb = 
 *   new GrammarBuilder&lt;...&gt;(nodeFactory, leafFactory);
 * Grammar&lt;...&gt; NUMBER = gb.token(CODE_NUMBER);
 * Grammar&lt;...&gt; product = gb.star(NUMBER);
 * Parser&lt;...&gt; parser = product.compile();
 * </pre>
 * 
 * <p>
 * This would define a {@code product} as arbitrary length sequence of
 * {@code NUMBER}s. When the parser recognizes {@code CODE_NUMBER}, it calls
 * the {@code leafFactory}. Later, when it has collected all {@code NUMBER}s
 * available, it will call the {@code nodeFactory} with the list of results
 * provided by the calls to {@code leafFactory}. The result of the parse
 * would be whatever the {@code nodeFactory} makes out of the list. It could
 * return an object that contains the list as a field, but it could as well
 * multiply the numbers and return an {@code N} that contains just the
 * product.
 * </p>
 * <p>
 * Slightly tricky is the creation of a recursive grammar. Create a
 * {@link Recurse} as a placeholder and later set its recursive child with
 * {@link Recurse#setChild(Grammar)}.
 * </p>
 * 
 * <p>
 * The use of the {@code GrammarBuilder} is recommended over direct use of
 * the grammar classes like {@link Sequence}, {@link Choice} and so on,
 * because it reliefs from a lot of generic parameter typing.
 * </p>
 * <h2>Parsing EOF</h2>
 * <p>
 * To make sure that parsers compiled from the grammars produced here parse
 * the whole input sequence, the lexer would need to eventually produce a
 * specific eof-token which is explicitly matched by the grammar. Suppose
 * your grammar is nothing but {@code G-> (SCOPE TERM)+}. The parser will
 * happily parse a non-empty sequence of {@code SCOPE} and {@code TERM}
 * pairs. In particular it will succeed for the sequence {@code SCOPE TERM
 * TERM} with parsing the first pair and will leave the 2nd {@code TERM}
 * unparsed. To prevent this, the grammar should rather be {@code G-> (SCOPE
 * TERM)+ EOF}.
 * 
 * 
 * <p>
 * <b>Remark:</b> This package has no support to create a
 * {@link absimpa.Lexer}, but there is a {@link example.TrivialLexer} the
 * source code of which may serve to demonstrate the principles.
 * </p>
 * 
 * @param <N> is the type of the objects created by the parser to be
 *        generated from the grammar constructed with a {@code
 *        GrammarBuilder}
 * @param <C> is the type of token codes provided by the lexer that will be
 *        used by parser
 * @param <L> is the type of {@link absimpa.Lexer} from which the parser
 *        obtains token information
 */
public class GrammarBuilder<N,C extends Enum<C>,L extends Lexer<C>> {
  private final NodeFactory<N> defaultNode;
  private final LeafFactory<N,C,L> defaultLeaf;
  /* +***************************************************************** */
  /**
   * <p>
   * the resulting {@code GrammarBuilder} will enter one of the given
   * factories into grammar objects as they are created, if no factory is
   * explicitly provided.
   * </p>
   */
  public GrammarBuilder(NodeFactory<N> defaultFactory,
                        LeafFactory<N,C,L> defaultLeaf) {
    this.defaultNode = defaultFactory;
    this.defaultLeaf = defaultLeaf;
  }
  /* +***************************************************************** */
  /**
   * <p>
   * creates a grammar to recognize a single token with code {@code code}.
   * The provided {@code factory} will be used to transform the recognized
   * token into the result type {@code N}.
   * </p>
   */
  public TokenGrammar<N,C,L> token(LeafFactory<N,C,L> factory, C code) {
    return new TokenGrammar<N,C,L>(factory, code);
  }
  /**
   * <p>
   * creates a grammar like {@link #token(LeafFactory, Enum)} but with the
   * default {@link LeafFactory}.
   * </p>
   */
  public TokenGrammar<N,C,L> token(C code) {
    return new TokenGrammar<N,C,L>(defaultLeaf, code);
  }
  /* +***************************************************************** */
  /**
   * <p>
   * creates a grammar to recognize a sequence of subgrammars which starts
   * with {@code g}. The provided {@code factory} will be used to transform
   * the recognized list of elements into the result type {@code N}. To add
   * more subgrammars to the sequence, use {@link Sequence#add}.
   * </p>
   */
  public Sequence<N,C,L> seq(NodeFactory<N> factory, Grammar<N,C,L> g) {
    return new Sequence<N,C,L>(factory, g);
  }
  /**
   * <p>
   * creates a grammar like {@link #seq(NodeFactory, Grammar)}, but with the
   * default {@link NodeFactory}.
   * </p>
   */
  public Sequence<N,C,L> seq(Grammar<N,C,L> g) {
    return new Sequence<N,C,L>(defaultNode, g);
  }
  /* +***************************************************************** */
  /**
   * creates a grammar to recognize a repetition of the given subgrammar
   * {@code g}. The provided {@code factory} will be used to transform the
   * recognized list of elements into the result type {@code N}.
   * 
   * @param min is the minimum number of times the subgrammar must be found
   *        in the input
   * @param max is the maximum number of times the subgrammar must be found
   *        in the input
   * @see #star
   * @see #opt
   */
  public Repeat<N,C,L> repeat(NodeFactory<N> factory, Grammar<N,C,L> g,
                              int min, int max)
  {
    return new Repeat<N,C,L>(factory, min, max, g);
  }
  /**
   * <p>
   * creates a grammar like {@link #repeat(NodeFactory, Grammar,int,int)},
   * but with the default {@link NodeFactory}.
   * </p>
   */
  public Repeat<N,C,L> repeat(Grammar<N,C,L> grammar, int min, int max) {
    return new Repeat<N,C,L>(defaultNode, min, max, grammar);
  }
  /**
   * convenience function to call {@link #repeat(Grammar,int,int)} with
   * {@code min=0} and {@code max=Integer.MAX_VALUE}.
   */
  public Repeat<N,C,L> star(Grammar<N,C,L> grammar) {
    return repeat(grammar, 0, Integer.MAX_VALUE);
  }
  /**
   * convenience function to call
   * {@link #repeat(NodeFactory,Grammar,int,int)} with {@code min=0} and
   * {@code max=Integer.MAX_VALUE}.
   */
  public Repeat<N,C,L> star(NodeFactory<N> nf, Grammar<N,C,L> grammar) {
    return repeat(nf, grammar, 0, Integer.MAX_VALUE);
  }
  /**
   * convenience function to call {@link #repeat(Grammar,int,int)} with
   * {@code min=0} and {@code max=1}.
   */
  public Repeat<N,C,L> opt(Grammar<N,C,L> grammar) {
    return repeat(defaultNode, grammar, 0, 1);
  }
  /**
   * convenience function to call
   * {@link #repeat(NodeFactory,Grammar,int,int)} with {@code min=0} and
   * {@code max=1}.
   */
  public Repeat<N,C,L> opt(NodeFactory<N> nf, Grammar<N,C,L> grammar) {
    return repeat(nf, grammar, 0, 1);
  }
  /* +***************************************************************** */
  /**
   * creates a grammar to recognize any one of a number of subgrammars, one
   * of which is the given {@code g}. To add more subgrammars to the choice,
   * call {@link Choice#or}. In contrast to all other grammars, the {@code
   * Choice} does not need a {@link NodeFactory}, because the resulting
   * parser just passes on the choice that was recognized.
   */
  public Choice<N,C,L> choice(Grammar<N,C,L> g) {
    return new Choice<N,C,L>(g);
  }
  /* +***************************************************************** */
}
