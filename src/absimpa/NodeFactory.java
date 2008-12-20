package absimpa;

import java.util.List;
/**
 * 
 * @param <N> is the type of node being created.
 */

public interface NodeFactory<N,C extends Enum<C>> {
  N create(List<N> children);
}
