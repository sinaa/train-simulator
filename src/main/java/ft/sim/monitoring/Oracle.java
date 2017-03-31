package ft.sim.monitoring;

import static ft.sim.monitoring.ViolationSeverity.CRITICAL;
import static ft.sim.monitoring.ViolationType.CRASH;

import ft.sim.train.Train;
import ft.sim.world.connectables.Section;
import ft.sim.world.journey.Journey;
import ft.sim.world.map.GlobalMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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

      if(duplicateSections.size()>0)
        logger.error("duplicates: {}", duplicateSections.size());

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
    Violation violation = new Violation(CRASH, CRITICAL, tick, violationDescription);
    addViolation(violation);
  }

}
