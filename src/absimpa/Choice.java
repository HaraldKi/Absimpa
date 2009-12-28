package absimpa;


import java.util.*;

import absimpa.parserimpl.ChoiceParser;


public class Choice<N,C extends Enum<C>>
    extends Grammar<N,C>
{
  private final List<Grammar<N,C>> choices;
  // private final Grammar<N,C> firstOptional;

  public Choice(Grammar<N,C> g) {
    choices = new ArrayList<Grammar<N,C>>();
    choices.add(g);
  }
  /* +***************************************************************** */
  public Choice<N,C> or(Grammar<N,C> g) {
    choices.add(g);
    return this;
  }
  /* +***************************************************************** */
  protected Iterable<Grammar<N,C>> children() {
    return Collections.unmodifiableList(choices);
  }
  /* +***************************************************************** */
  protected Parser<N,C> buildParser(Map<Grammar<N,C>,First<N,C>> firstOf) {
    EnumMap<C,Parser<N,C>> choiceMap = buildMap(firstOf);
    First<N,C> f = first(firstOf);
    return new ChoiceParser<N,C>(f.epsilon, choiceMap);
  }
  /* +***************************************************************** */
  private EnumMap<C,Parser<N,C>> buildMap(
                                          Map<Grammar<N,C>,First<N,C>> firstOf)
  {
    EnumMap<C,Parser<N,C>> result = null;

    for(Grammar<N,C> g : choices) {
      First<N,C> f = g.first(firstOf);
      Parser<N,C> p = g.build(firstOf);
      if( result==null ) result = initMap(f.firstSet());
      addToMap(result, f.firstSet(), p);
    }
    return result;
  }
  /* +***************************************************************** */
  private EnumMap<C,Parser<N,C>> initMap(EnumSet<C> codes) {
    // if( codes.isEmpty() ) codes = EnumSet.complementOf(codes);
    for(C code : codes) {
      return new EnumMap<C,Parser<N,C>>(code.getDeclaringClass());
    }
    throw new RuntimeException(
        "this happens only, if a firstSet is empty, which should"
            +" never be the case");
  }
  /* +***************************************************************** */
  private void addToMap(EnumMap<C,Parser<N,C>> map, EnumSet<C> codes,
                        Parser<N,C> p)
  {
    for(C code : codes) {
      if( map.containsKey(code) ) throw new RuntimeException("not possible");
      map.put(code, p);
    }
  }
  /* +***************************************************************** */
  protected First<N,C> computeFirst(Map<Grammar<N,C>,First<N,C>> firstOf) {
    boolean optional = false;
    EnumSet<C> firstSet = null;

    for(Grammar<N,C> ga: choices) {
      First<N,C> f = ga.first(firstOf);
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
    return new First<N,C> (firstSet, optional);
  }
  /* +***************************************************************** */
}
