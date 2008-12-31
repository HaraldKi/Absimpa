package absimpa;


import java.util.*;

import absimpa.parserimpl.ChoiceParser;


public class Choice<N,C extends Enum<C>,L extends Lexer<C>>
    extends Grammar<N,C,L>
{
  private final List<Grammar<N,C,L>> choices;
  // private final Grammar<N,C,L> firstOptional;

  public Choice(Grammar<N,C,L> g) {
    choices = new ArrayList<Grammar<N,C,L>>();
    choices.add(g);
  }
  /* +***************************************************************** */
  public Choice<N,C,L> or(Grammar<N,C,L> g) {
    choices.add(g);
    return this;
  }
  /* +***************************************************************** */
  protected Iterable<Grammar<N,C,L>> children() {
    return Collections.unmodifiableList(choices);
  }
  /* +***************************************************************** */
  protected Parser<N,C,L> buildParser(Map<Grammar<N,C,L>,First<N,C,L>> firstOf) {
    EnumMap<C,Parser<N,C,L>> choiceMap = buildMap(firstOf);
    First<N,C,L> f = first(firstOf);
    return new ChoiceParser<N,C,L>(f.epsilon, choiceMap);
  }
  /* +***************************************************************** */
  private EnumMap<C,Parser<N,C,L>> buildMap(
                                          Map<Grammar<N,C,L>,First<N,C,L>> firstOf)
  {
    EnumMap<C,Parser<N,C,L>> result = null;

    for(Grammar<N,C,L> g : choices) {
      First<N,C,L> f = g.first(firstOf);
      Parser<N,C,L> p = g.build(firstOf);
      if( result==null ) result = initMap(f.firstSet());
      addToMap(result, f.firstSet(), p);
    }
    return result;
  }
  /* +***************************************************************** */
  private EnumMap<C,Parser<N,C,L>> initMap(EnumSet<C> codes) {
    // if( codes.isEmpty() ) codes = EnumSet.complementOf(codes);
    for(C code : codes) {
      return new EnumMap<C,Parser<N,C,L>>(code.getDeclaringClass());
    }
    throw new RuntimeException(
        "this happens only, if a firstSet is empty, which should"
            +" never be the case");
  }
  /* +***************************************************************** */
  private void addToMap(EnumMap<C,Parser<N,C,L>> map, EnumSet<C> codes,
                        Parser<N,C,L> p)
  {
    for(C code : codes) {
      if( map.containsKey(code) ) throw new RuntimeException("not possible");
      map.put(code, p);
    }
  }
  /* +***************************************************************** */
  protected First<N,C,L> computeFirst(Map<Grammar<N,C,L>,First<N,C,L>> firstOf) {
    boolean optional = false;
    EnumSet<C> firstSet = null;
    for(Grammar<N,C,L> g : choices) {
      Grammar<N,C,L> ga = (Grammar<N,C,L>)g;
      First<N,C,L> f = ga.first(firstOf);
      if( firstSet==null ) {
        firstSet = f.firstSet();
      } else {
        EnumSet<C> otherFirst = f.firstSet();
        if( firstSet.removeAll(otherFirst) ) {
          throw lookaheadConflict(choices, ga, firstOf);
        }
        firstSet.addAll(f.firstSet());
      }
      optional |= f.epsilon;
    }
    return new First<N,C,L>(firstSet, optional);
  }
  /* +***************************************************************** */
}
