package ft.sim.monitoring;

import static ft.sim.monitoring.ViolationSeverity.CRITICAL;

import ft.sim.world.train.Train;
import ft.sim.world.connectables.Connectable;
import ft.sim.world.connectables.Section;
import ft.sim.world.connectables.Station;
import ft.sim.world.connectables.Switch;
import ft.sim.world.connectables.Track;
import ft.sim.world.journey.Journey;
import ft.sim.world.map.GlobalMap;
import ft.sim.world.map.MapBuilderHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sina on 21/03/2017.
 */
public class Oracle {

  protected static transient Logger logger = LoggerFactory.getLogger(Oracle.class);

  private GlobalMap world;
  private long tick;

  private List<Violation> violations = new ArrayList<>();

  public List<Violation> getViolations() {
    return violations;
  }

  public void addViolation(Violation violation) {
    violations.add(violation);
    if (violation.getSeverity() == CRITICAL) {
      throw new CriticalViolationException(violation);
    }
  }

  public void checkState(GlobalMap world, long tick) throws CriticalViolationException {
    this.world = world;
    this.tick = tick;

    checkForTrainCollisions();
    ensureStationCapacity();
    if (world.isConfiguration("mode", "fixed_block")) {
      ensureOneTrainPerTrackOrSwitch();
    }
    if (world.isConfiguration("mode", "variable_block")) {
      ensureTrainsWithinEmergencyBreakingDistance();
    }
  }

  private void ensureTrainsWithinEmergencyBreakingDistance() {
    Map<Journey, Journey> trainMap = MapBuilderHelper.getJourneysFollowingEachOther(world);
    for (Entry<Journey, Journey> pair : trainMap.entrySet()) {
      Journey j1 = pair.getKey();
      Journey j2 = pair.getValue();
      if (j1.getJourneyPosition().getConnectablesOccupied().stream()
          .anyMatch(c -> c instanceof Station)
          || j2.getJourneyPosition().getConnectablesOccupied().stream()
          .anyMatch(c -> c instanceof Station)) {
        continue;
      }
      double distance = MapBuilderHelper.getJourneyDistance(j2, j1);

      double breakingDistance = j1.getTrain().getEcu().calculateBreakingDistance();

      if (breakingDistance > distance) {
        logger.info("{} and {}, distance: {} , breaking distance:{}", j1, j2, distance, breakingDistance);
        ViolationBuilder.createVariableBlockViolation(this, j1.getTrain(), j2.getTrain(), distance);
      }
    }
  }

  private void ensureOneTrainPerTrackOrSwitch() {
    Map<Connectable, Train> occupiedConnectables = new HashMap<>();

    Map<Integer, Journey> journeys = world.getJourneys();
    for (Entry<Integer, Journey> j : journeys.entrySet()) {
      Journey journey = j.getValue();
      List<Connectable> connectables = j.getValue().getJourneyPosition().getConnectablesOccupied()
          .stream().filter(c -> c instanceof Track || c instanceof Switch)
          .collect(Collectors.toList());

      List<Connectable> duplicateConnectables = connectables.stream()
          .filter(occupiedConnectables::containsKey).collect(Collectors.toList());

      duplicateConnectables.forEach(c -> ViolationBuilder
          .createFixedBlockViolation(this, journey.getTrain(), occupiedConnectables.get(c), c));

      connectables.forEach(c -> occupiedConnectables.put(c, journey.getTrain()));
    }
  }

  private void ensureStationCapacity() {
    world.getStations().values().stream()
        .filter(station -> station.getCapacity() < station.usedCapacity())
        .forEach(s -> ViolationBuilder.createOverfullStationViolation(this, s));
  }

  private void checkForTrainCollisions() {
    Map<Section, Train> occupiedSections = new HashMap<>();

    Map<Integer, Journey> journeys = world.getJourneys();
    for (Entry<Integer, Journey> j : journeys.entrySet()) {
      Journey journey = j.getValue();
      List<Section> sectionsOccupied = journey.getJourneyPosition().getSectionsOccupied();

      List<Section> duplicateSections = sectionsOccupied.stream().
          filter(occupiedSections::containsKey).collect(Collectors.toList());

      duplicateSections.forEach(s -> ViolationBuilder
          .createTrainCrashViolation(this, journey.getTrain(), occupiedSections.get(s), s));

      if (duplicateSections.size() > 0) {
        logger.error("duplicates: {}", duplicateSections.size());
      }

      sectionsOccupied.forEach(s -> occupiedSections.put(s, journey.getTrain()));
    }
  }


  public GlobalMap getWorld() {
    return world;
  }

  public long getTick() {
    return tick;
  }
}
