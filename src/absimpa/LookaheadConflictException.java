package absimpa;

import java.util.*;

/**
 * thrown by {@link Grammar#compile}.
 */
public class LookaheadConflictException extends RuntimeException {

  public LookaheadConflictException(EnumSet la, Grammar g1, Grammar g2) {
    super(makeMsg(la, g1, g2));
  }
  private static String makeMsg(EnumSet la, Grammar g1, Grammar g2) {
    return String.format("conflicting lookahead %s for "+
                         "grammars %s and %s",
                         la, g1, g2);
  }
}
