package absimpa;

import java.util.List;
/**
 * <p>
 * creates a new {@code N} from a list of {@code N} objects provided by a
 * parser. Whenever a parser recognizes a piece of input, the sub-pieces
 * where already mapped to objects of type {@code N}. The list of these
 * objects is passed by the parser to its {@link NodeFactory} to obtain a
 * combined {@code N}.
 * </p>
 * 
 * @param <N> is the type of node being created.
 */

public interface NodeFactory<N> {
  /**
   * <p>
   * creates a new {@code N} from the child objects. This list of children
   * may be empty, but it will not contain {@code null} elements.
   * </p>
   * 
   * @return an {@code N} or {@code null}. A return value of {@code null}
   *         will not be put into a list of nodes on the level above, but
   *         will be discarded.
   */
  N create(List<N> children);
}
