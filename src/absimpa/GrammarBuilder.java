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
 * the parser has recognized a token, it will ask the lexer to provide a leaf
 * node for it. And when it has recognized a partial bit of input, it will
 * call a {@link NodeFactory} with the bits it has recognized and asks it to
 * create an {@code N}.</li>
 * 
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
 * {@link Choice#or Choice.or()} to create a choice of subnodes. A rough
 * outline of an example is:
 * </p>
 * 
 * <pre>
 * GrammarBuilder&lt;...&gt; gb = 
 *   new GrammarBuilder&lt;...&gt;(nodeFactory);
 * Grammar&lt;...&gt; NUMBER = gb.token(CODE_NUMBER);
 * Grammar&lt;...&gt; product = gb.star(NUMBER);
 * Parser&lt;...&gt; parser = product.compile();
 * </pre>
 * 
 * <p>
 * This would define a {@code product} as arbitrary length sequence of
 * {@code NUMBER}s. When the parser has collected all {@code NUMBER}s
 * available, it will call the {@code nodeFactory} with the list of results
 * provided by the lexer to the token parser. The result of the parse would
 * be whatever the {@code nodeFactory} makes out of the list. It could return
 * an object that contains the list as a field, but it could as well multiply
 * the numbers and return an {@code N} that contains just the product.
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
 * the whole input sequence, the lexer eventually needs to produce a specific
 * eof-token which is explicitly matched by the grammar. Suppose your grammar
 * is nothing but {@code G-> (SCOPE TERM)+}. The parser will happily parse a
 * non-empty sequence of {@code SCOPE} and {@code TERM} pairs. In particular
 * it will succeed for the sequence {@code SCOPE TERM TERM} with parsing the
 * first pair and will leave the 2nd {@code TERM} unparsed. To prevent this,
 * the grammar should rather be {@code G-> (SCOPE TERM)+ EOF}.
 * 
 * <h2>Multiple Argument Methods</h2>
 * <p>
 * It would be nice to use varargs methods for {@code seq()} and {@code
 * choice()}. But due to the generic parameters of grammars, there would then
 * always be the compiler warning about unsafe conversion to array. Therefore
 * there are variants of those methods with up to 5 parameters. Only beyond
 * that, varargs is used.</p>
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
 */
public class GrammarBuilder<N,C extends Enum<C>> {
  private final NodeFactory<N> defaultNode;
  /* +***************************************************************** */
  /**
   * <p>
   * the resulting {@code GrammarBuilder} will enter the given factory into
   * grammar objects as they are created, if no factory is explicitly
   * provided.
   * </p>
   */
  public GrammarBuilder(NodeFactory<N> defaultFactory) {
    this.defaultNode = defaultFactory;
  }
  /* +***************************************************************** */
  /**
   * <p>
   * creates a grammar to recognize the given token code.
   * </p>
   */
  public TokenGrammar<N,C> token(C code) {
    return new TokenGrammar<N,C>(code);
  }
  /* +***************************************************************** */
  /**
   * <p>
   * creates a grammar to recognize a sequence of subgrammars which starts
   * with {@code g}. The {@code factory} provided will be used to transform
   * the recognized list of elements into the result type {@code N}. To add
   * more subgrammars to the sequence, use {@link Sequence#add}.
   * </p>
   */
  public Sequence<N,C> seq(NodeFactory<N> factory, Grammar<N,C> g) {
    Sequence<N,C> s = new Sequence<N,C>(g);
    s.setNodeFactory(factory);
    return s;
  }
  /**
   * <p>
   * creates a grammar like {@link #seq(NodeFactory, Grammar)}, but with the
   * default {@link NodeFactory}.
   * </p>
   */
  public Sequence<N,C> seq(Grammar<N,C> g) {
    return seq(defaultNode, g);
  }
  /* +***************************************************************** */
  public Sequence<N,C> seq(Grammar<N,C> g1, Grammar<N,C> g2) {
    return seq(g1).add(g2);
  }
  public Sequence<N,C> seq(Grammar<N,C> g1, Grammar<N,C> g2, Grammar<N,C> g3)
  {
    return seq(g1, g2).add(g3);
  }
  public Sequence<N,C> seq(Grammar<N,C> g1, Grammar<N,C> g2,
                           Grammar<N,C> g3, Grammar<N,C> g4)
  {
    return seq(g1,g2,g3).add(g4);
  }
  public Sequence<N,C> seq(Grammar<N,C> g1, Grammar<N,C> g2, Grammar<N,C> g3,
                           Grammar<N,C> g4, Grammar<N,C> g5) {
    return seq(g1,g2,g3,g4).add(g5);
  }
  public Sequence<N,C> seq(Grammar<N,C> g1, Grammar<N,C> g2, Grammar<N,C> g3,
                           Grammar<N,C> g4, Grammar<N,C> g5, 
                           Grammar<N,C> ... more) {
    Sequence<N,C> s =  seq(g1,g2,g3,g4).add(g5);
    for(Grammar<N,C> g : more) s.add(g);
    return s;
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
  public Repeat<N,C> repeat(NodeFactory<N> factory, Grammar<N,C> g, int min,
                            int max)
  {
    Repeat<N,C> rep =  new Repeat<N,C>(min, max, g);
    rep.setNodeFactory(factory);
    return rep;
  }
  /**
   * <p>
   * creates a grammar like {@link #repeat(NodeFactory, Grammar,int,int)},
   * but with the default {@link NodeFactory}.
   * </p>
   */
  public Repeat<N,C> repeat(Grammar<N,C> grammar, int min, int max) {
    return repeat(defaultNode, grammar, min, max);
  }
  /**
   * convenience function to call {@link #repeat(Grammar,int,int)} with
   * {@code min=0} and {@code max=Integer.MAX_VALUE}.
   */
  public Repeat<N,C> star(Grammar<N,C> grammar) {
    return repeat(grammar, 0, Integer.MAX_VALUE);
  }
  /**
   * convenience function to call
   * {@link #repeat(NodeFactory,Grammar,int,int)} with {@code min=0} and
   * {@code max=Integer.MAX_VALUE}.
   */
  public Repeat<N,C> star(NodeFactory<N> nf, Grammar<N,C> grammar) {
    return repeat(nf, grammar, 0, Integer.MAX_VALUE);
  }
  /**
   * convenience function to call {@link #repeat(Grammar,int,int)} with
   * {@code min=0} and {@code max=1}.
   */
  public Repeat<N,C> opt(Grammar<N,C> grammar) {
    return repeat(defaultNode, grammar, 0, 1);
  }
  /**
   * convenience function to call
   * {@link #repeat(NodeFactory,Grammar,int,int)} with {@code min=0} and
   * {@code max=1}.
   */
  public Repeat<N,C> opt(NodeFactory<N> nf, Grammar<N,C> grammar) {
    return repeat(nf, grammar, 0, 1);
  }
  /* +***************************************************************** */
  /**
   * creates a grammar to recognize any one of a number of sub grammars, one
   * of which is the given {@code g}. To add more sub grammars to the choice,
   * call {@link Choice#or}. In contrast to all other grammars, the {@code
   * Choice} does not need a {@link NodeFactory}, because the resulting
   * parser just passes on the choice that was recognized.
   */
  public Choice<N,C> choice(Grammar<N,C> g) {
    return new Choice<N,C>(g);
  }
  public Choice<N,C> choice(Grammar<N,C> g1, Grammar<N,C> g2) {
    return choice(g1).or(g2);
  }
  public Choice<N,C> choice(Grammar<N,C> g1, Grammar<N,C> g2, Grammar<N,C> g3) {
    return choice(g1).or(g2).or(g3);
  }
  public Choice<N,C> choice(Grammar<N,C> g1, Grammar<N,C> g2, Grammar<N,C> g3,
                            Grammar<N,C> g4) {
    return choice(g1).or(g2).or(g3).or(g4);
  }
  public Choice<N,C> choice(Grammar<N,C> g1, Grammar<N,C> g2, Grammar<N,C> g3,
                            Grammar<N,C> g4, Grammar<N,C> g5) {
    return choice(g1).or(g2).or(g3).or(g4).or(g5);
  }
  public Choice<N,C> choice(Grammar<N,C> g1, Grammar<N,C> g2, Grammar<N,C> g3,
                            Grammar<N,C> g4, Grammar<N,C> g5,
                            Grammar<N,C> ... more) {
    Choice<N,C> c = choice(g1).or(g2).or(g3).or(g4).or(g5);
    for(Grammar<N,C> g : more) c.or(g);
    return c;    
  }
  /* +***************************************************************** */
}
