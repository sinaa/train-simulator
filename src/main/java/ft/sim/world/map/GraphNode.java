package ft.sim.world.map;

import ft.sim.world.connectables.Connectable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Sina on 03/04/2017.
 */
public class GraphNode {

  private Connectable connectable;
  private Set<GraphNode> edges = new HashSet<>();

  GraphNode(Connectable connectable) {
    this.connectable = connectable;
    //edges.put(connectable,new HashSet<>());
  }

  public Connectable getParent() {
    return connectable;
  }

  public boolean isLeaf() {
    return edges.size() == 0;
  }

  public Set<GraphNode> getEdges() {
    return edges;
  }

  private void addEdge(Connectable connectable) {
    GraphNode node = new GraphNode(connectable);
    edges.add(node);
  }

  public boolean addEdge(Connectable parent, Connectable child, Set<GraphNode> covered) {
    boolean added = false;
    if (covered == null) {
      covered = new HashSet<>();
    }
    if (parent.equals(this.connectable)) {
      addEdge(child);
      added = true;
    } else {
      if (covered.contains(this)) {
        return false;
      }
      for (GraphNode childNode : this.edges) {
        boolean childAdded = childNode.addEdge(parent, child, covered);
        covered.add(childNode);
        if (added) {
          added = true;
          break;
        }
      }
    }
    return added;
  }


}
