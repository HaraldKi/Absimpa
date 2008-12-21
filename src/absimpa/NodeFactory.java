package absimpa;

import java.util.List;
/**
 * <p>
 * creates a new {@code N} from a list of {@code N} objects provided by a
 * absimpa. Whenever a absimpa recognizes a piece of input, the sub-pieces
 * where already mapped to objects of type {@code N}. The list of these
 * objects is passed by the absimpa to its {@link NodeFactory} to obtain a combined
 * {@code N}.</p>
 * 
 * @param <N> is the type of node being created.
 */

public interface NodeFactory<N> {
  /**
   * <p>
   * must create a new {@code N} from the child objects.
   * </p>
   */
  N create(List<N> children);
}
