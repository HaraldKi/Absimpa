package absimpa.parserimpl;

import java.util.EnumSet;
import java.util.List;

import absimpa.*;

public abstract class AbstractParser<N,C extends Enum<C>>
    implements Parser<N,C>
{
  protected String name = null;
  protected final EnumSet<C> lookahead;
  protected final boolean mayBeEpsilon;
  private NodeFactory<N> nodeFactory;
  
  /*+******************************************************************/
  AbstractParser(EnumSet<C> lookahead, boolean mayBeEpsilon) {
    this.lookahead = lookahead;
    this.mayBeEpsilon = mayBeEpsilon;
    this.nodeFactory = null;
  }
  /*+******************************************************************/
  public void setName(String name) { this.name = name; }
  /*+******************************************************************/
  public AbstractParser<N,C> setNodeFactory(NodeFactory<N> nf) {
    this.nodeFactory = nf;
    return this;
  }
  /*+******************************************************************/
  @Override
  public N parse(Lexer<N,C> lex) throws ParseException {
    ParseResult<N> result = parseInternal(lex);
    if( result.isEpsilon() ) return null;
    if( result.notApplicable() ) {
      throw lex.parseException(lookahead);
    }
    return result.getNode();
  }
  /*+******************************************************************/
  EnumSet<C> getLookahead() {
    return EnumSet.copyOf(lookahead);
  }
  /*+******************************************************************/
  final ParseResult<N> parseInternal (Lexer<N,C> lex) throws ParseException {
    if( !lookahead.contains(lex.current()) ) {
      if( mayBeEpsilon ) {
        return ParseResult.ISEPSILON();
      }
      return ParseResult.NOTAPPLICABLE();
    }
    
    List<N> nodes = doParse(lex);
    
    if( nodeFactory==null ) {
      return new ParseResult<>(nodes);
    } 
    return new ParseResult<>(nodeFactory.create(nodes));
  }
  /*+******************************************************************/
  abstract List<N> doParse(Lexer<N,C> lex)
    throws ParseException;
  /*+******************************************************************/
  private String shortClassname() {
    String className = getClass().getName();
    int p = className.lastIndexOf('.');
    if( p<0 ) return className;
    return className.substring(p+1, className.length());
  }
  /* +***************************************************************** */
  String getName() {
    if( name==null ) {
      return shortClassname();
    }
    return name;
  }
  public String toString() {
    return getName();
  }
}
