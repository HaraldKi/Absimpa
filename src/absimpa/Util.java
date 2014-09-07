package absimpa;

import java.util.Iterator;

/**
 * the typical set of static utility methods.
 */
public class Util {
  private Util(){}
  /**
   * join a list of things into a string with a prefix, a separator and a
   * suffix.
   */
  public static String join(Iterable<?> list, String prefix, String sep,
                            String suffix)
  {
    StringBuilder sb = new StringBuilder();
    sb.append(prefix);
    Iterator<?> it = list.iterator();
    if( it.hasNext() ) {
      sb.append(it.next());
    }
    while( it.hasNext() ) {
      sb.append(sep).append(it.next());
    }
    sb.append(suffix);
    return sb.toString();
  }
  /*+******************************************************************/
}
