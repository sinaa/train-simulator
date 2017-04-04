package ft.sim.world.map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ft.sim.world.connectables.Connectable;
import ft.sim.world.connectables.Track;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Sina on 03/04/2017.
 */
public class MapGraph {

  private Multimap<Connectable, Connectable> graph = HashMultimap.create();
  private Multimap<Connectable, Connectable> graphInverse = HashMultimap.create();

  private Set<GraphNode> roots = new HashSet<>();

  private boolean isBuilt = false;

  public Collection<Connectable> getChildren(Connectable connectable) {
    return graph.get(connectable);
  }

  public Collection<Connectable> getParents(Connectable connectable) {
    return graphInverse.get(connectable);
  }

  public boolean isBuilt() {
    return isBuilt;
  }

  public void buildGraph() {
    if (isBuilt) {
      throw new IllegalStateException("Graph was already built!");
    }
    flattenGraph(roots);
    verifyGraph();
    isBuilt = true;
  }

  public void addEdge(Connectable from, Connectable to) {
    if (from == null) {
      roots.add(new GraphNode(to));
      return;
    }
    for (GraphNode node : roots) {
      if (node.addEdge(from, to, null)) {
        return;
      }
    }
  }

  private void flattenGraph(Set<GraphNode> nodes) {
    for (GraphNode node : nodes) {
      Connectable parent = node.getParent();
      for (GraphNode child : node.getEdges()) {
        graph.put(parent, child.getParent());
        graphInverse.put(child.getParent(), parent);
      }
      if (node.isLeaf()) {
        graph.put(parent, null);
      } else {
        flattenGraph(node.getEdges());
      }
    }
    for (GraphNode root : roots) {
      graphInverse.put(null, root.getParent());
    }

    for (Entry<Connectable, Collection<Connectable>> graphEntry : graph.asMap().entrySet()) {
      Collection<Connectable> values = graphEntry.getValue();
      if (values.size() > 1 && values.contains(null)) {
        graph.remove(graphEntry.getKey(), null);
      }
    }
    for (Entry<Connectable, Collection<Connectable>> graphEntry : graphInverse.asMap().entrySet()) {
      Collection<Connectable> values = graphEntry.getValue();
      if (values.size() > 1 && values.contains(null)) {
        graphInverse.remove(graphEntry.getKey(), null);
      }
    }
  }

  public Set<GraphNode> getRoots() {
    return roots;
  }

  public Set<Connectable> getRootConnectables() {
    return roots.stream().map(GraphNode::getParent).collect(Collectors.toSet());
  }

  public Track getFirstTrack(Connectable rootConnectable) {
    if (rootConnectable instanceof Track) {
      return (Track) rootConnectable;
    }
    for (Connectable connectable : getChildren(rootConnectable)) {
      Track firstTrack = getFirstTrack(connectable);
      if (firstTrack != null) {
        return firstTrack;
      }
    }
    return null;
  }

  /**
   * Sanity checks for the graph
   */
  private void verifyGraph() {
    for (Entry<Connectable, Collection<Connectable>> connectable : graph.asMap().entrySet()) {
      if (connectable.getKey() instanceof Track && connectable.getValue().size() > 1) {
        throw new IllegalStateException(
            "A track cannot be connected to more than one placeable. Track: "
                + connectable.getKey());
      }
    }
  }
}
