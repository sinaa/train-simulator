package ft.sim.world.map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ft.sim.world.connectables.Connectable;
import ft.sim.world.connectables.Station;
import ft.sim.world.connectables.Track;
import ft.sim.world.journey.Journey;
import ft.sim.world.journey.JourneyPath;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sina on 03/04/2017.
 */
public class MapGraph {

  protected transient static final Logger logger = LoggerFactory.getLogger(MapGraph.class);

  private Multimap<Connectable, Connectable> graph = HashMultimap.create();
  private Multimap<Connectable, Connectable> graphInverse = HashMultimap.create();

  private Set<GraphNode> roots = new LinkedHashSet<>();

  private boolean isBuilt = false;

  public Collection<Connectable> getChildren(Connectable connectable) {
    return graph.get(connectable);
  }

  public Connectable getNexTConnectable(Connectable connectable) {
    Collection<Connectable> next = graph.get(connectable);
    if (next.stream().filter(Objects::nonNull).count() > 1) {
      throw new IllegalStateException("The next item shouldn't have multiple children");
    }
    return next.stream().map(Optional::ofNullable).findFirst()
        .flatMap(Function.identity())
        .orElse(null);
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
    GraphNode potentialParent = getRootGraphNode(to);
    if (potentialParent != null) {
      roots.remove(potentialParent);
      GraphNode newRootParent = new GraphNode(from);
      newRootParent.addEdge(potentialParent);

      roots.add(newRootParent);
      return;
    }

    for (GraphNode node : roots) {
      if (from == null && node.hasEdge(to)) {
        return;
      }
      if (node.addEdge(from, to, null)) {
        return;
      }
    }
    if (from == null) {
      roots.add(new GraphNode(to));
      logger.warn("adding {} root", to.getClass().getSimpleName());
      return;
    }
  }

  private GraphNode getRootGraphNode(Connectable c) {
    for (GraphNode node : roots) {
      if (node.getParent() == c) {
        return node;
      }
    }
    return null;
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
      Connectable from = connectable.getKey();
      if (from instanceof Track && connectable.getValue().size() > 1) {
        throw new IllegalStateException(
            "A track cannot be connected to more than one placeable. Track: " + from);
      }
      if (from instanceof Station && connectable.getValue().size() > 1) {
        throw new IllegalStateException(
            "A station cannot be connected to more than one placeable. Station: " + from);
      }
      if (from instanceof Station && connectable.getValue().stream().filter(Objects::nonNull)
          .anyMatch(c -> !(c instanceof Track))) {
        throw new IllegalStateException(
            "A station cannot be connected to something other than a track. Station: " + from);
      }
    }
  }


  public Iterator<Connectable> getIterator(Connectable root) {
    Iterator<Connectable> it = new Iterator<Connectable>() {

      private final Connectable rootNode = root;
      private Connectable currentNode = null;

      @Override
      public boolean hasNext() {
        if (currentNode == null) {
          return true;
        }

        Collection<Connectable> children = getChildren(currentNode);

        if (children.size() > 1) {
          throw new IllegalStateException(
              "Branching rail structure isn't allowed. Children: " + children.size());
        }

        return !children.isEmpty() && (children.iterator().next() != null);
      }

      @Override
      public Connectable next() {
        if (currentNode == null) {
          currentNode = rootNode;
          return currentNode;
        }

        //Connectable tmpCurrentNode = currentNode;
        Connectable nextNode = getChildren(currentNode).iterator().next();
        if (nextNode == null) {
          throw new NoSuchElementException();
        }

        currentNode = nextNode;
        return currentNode;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
    return it;
  }

  public Multimap<Connectable, Connectable> getConnectablesGraph() {
    return graph;
  }

  public void initJourney(JourneyPath journeyPath) {
    Connectable globalRoot = getRootNodeForJourney(journeyPath).getParent();
    Connectable journeyRoot = journeyPath.getFirst();

    journeyPath.setGraphRootConnectable(globalRoot);

    if (globalRoot == journeyRoot) {
      return;
    }

    Connectable node = globalRoot;
    double length = 0;
    while (node != journeyRoot) {
      length += node.getLength();
      try {
        node = graph.get(node).stream().iterator().next();
      } catch (NoSuchElementException e) {
        throw new IllegalStateException("Reached the end of the graph without finding node");
      }
    }
    journeyPath.setDistanceFromGraphRoot(length);
  }

  private GraphNode getRootNodeForJourney(JourneyPath journeyPath) {
    Connectable connectable = journeyPath.getFirst();
    for (GraphNode root : roots) {
      if (root.getParent() == connectable || root.hasEdge(connectable)) {
        return root;
      }
    }
    throw new IllegalStateException("The journey's root doesn't exist in the graph!");
  }


}
