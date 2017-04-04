package ft.sim.monitoring;

import static ft.sim.monitoring.ViolationSeverity.CRITICAL;
import static ft.sim.monitoring.ViolationType.CRASH;

import ft.sim.train.Train;
import ft.sim.world.connectables.Connectable;
import ft.sim.world.connectables.Section;
import ft.sim.world.connectables.Switch;
import ft.sim.world.connectables.Track;
import ft.sim.world.journey.Journey;
import ft.sim.world.map.GlobalMap;
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

  protected static Logger logger = LoggerFactory.getLogger(Oracle.class);

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
    if (world.isConfiguration("mode", "fixed_block")) {
      ensureOneTrainPerTrackOrSwitch();
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

      duplicateConnectables.forEach(
          c -> createFixedBlockViolation(journey.getTrain(), occupiedConnectables.get(c), c));

      connectables.forEach(c->occupiedConnectables.put(c,journey.getTrain()));
    }
  }

  private void checkForTrainCollisions() {
    Map<Section, Train> occupiedSections = new HashMap<>();

    Map<Integer, Journey> journeys = world.getJourneys();
    for (Entry<Integer, Journey> j : journeys.entrySet()) {
      Journey journey = j.getValue();
      List<Section> sectionsOccupied = journey.getJourneyPosition().getSectionsOccupied();

      List<Section> duplicateSections = sectionsOccupied.stream().
          filter(occupiedSections::containsKey).collect(Collectors.toList());

      duplicateSections
          .forEach(s -> createTrainCrashViolation(journey.getTrain(), occupiedSections.get(s), s));

      if (duplicateSections.size() > 0) {
        logger.error("duplicates: {}", duplicateSections.size());
      }

      sectionsOccupied.forEach(s -> occupiedSections.put(s, journey.getTrain()));
    }
  }

  private void createTrainCrashViolation(Train t1, Train t2, Section section) {
    int idTrain1 = world.getTrainID(t1);
    int idTrain2 = world.getTrainID(t2);
    int trackID = world.getTrackIDforSection(section);
    int sectionID = world.getTrack(trackID).getSectionPosition(section);
    String violationDescription = String
        .format("Train %s and Train %s were on the same Section %s on track %s",
            idTrain1, idTrain2, sectionID, trackID);
    addViolation(new Violation(CRASH, CRITICAL, tick, violationDescription));
  }

  private void createFixedBlockViolation(Train t1, Train t2, Connectable connectable) {
    String connectableName = "Unknown";
    if (connectable instanceof Track) {
      connectableName = "Track-" + world.getTrackID((Track) connectable);
    } else if (connectable instanceof Switch) {
      connectableName = "Switch-" + world.getSwitchID((Switch) connectable);
    }

    String violationDescription = String
        .format("Train %s and Train %s were on the same Connectable %s",
            world.getTrainID(t1), world.getTrainID(t2), connectableName);
    addViolation(new Violation(CRASH, CRITICAL, tick, violationDescription));
  }

}
