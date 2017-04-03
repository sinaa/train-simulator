package ft.sim.world.map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ft.sim.world.connectables.Connectable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Sina on 03/04/2017.
 */
public class MapGraph {

  private Multimap<Connectable, Connectable> graph = HashMultimap.create();
  private Multimap<Connectable, Connectable> graphInverse = HashMultimap.create();

  public Collection<Connectable> getChildren(Connectable connectable) {
    return graph.get(connectable);
  }

  public Collection<Connectable> getParents(Connectable connectable) {
    return graphInverse.get(connectable);
  }

  Set<GraphNode> roots = new HashSet<>();

  public void buildGraph() {
    flattenGraph(roots);
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
}
