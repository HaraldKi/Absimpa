package absimpa;

import java.util.Iterator;


public class Util {
  public static String join(Iterable<?> list, String start, String sep, String end) {
    StringBuilder sb = new StringBuilder();
    sb.append(start);
    Iterator<?> it = list.iterator();
    if( it.hasNext() ) {
      sb.append(it.next());
    }
    while( it.hasNext() ) {
      sb.append(sep).append(it.next());
    }
    sb.append(end);
    return sb.toString();
  }
  /*+******************************************************************/
  public static String join(Iterable<?> list, String sep) {
    return join(list, "", sep, "");
  }
}
