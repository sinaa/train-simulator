package ft.sim.monitoring;

import static ft.sim.monitoring.ViolationSeverity.*;
import static ft.sim.monitoring.ViolationType.*;

import ft.sim.train.Train;
import ft.sim.world.connectables.Connectable;
import ft.sim.world.connectables.Section;
import ft.sim.world.connectables.Station;
import ft.sim.world.connectables.Switch;
import ft.sim.world.connectables.Track;

/**
 * Created by sina on 10/04/2017.
 */
public class ViolationBuilder {

  public static void createTrainCrashViolation(Oracle o, Train t1, Train t2, Section section) {
    int idTrain1 = o.getWorld().getTrainID(t1);
    int idTrain2 = o.getWorld().getTrainID(t2);
    int trackID = o.getWorld().getTrackIDforSection(section);
    int sectionID = o.getWorld().getTrack(trackID).getSectionPosition(section);
    String violationDescription = String
        .format("Train %s and Train %s were on the same Section %s on track %s",
            idTrain1, idTrain2, sectionID, trackID);

    o.addViolation(new Violation(CRASH, CRITICAL, o.getTick(), violationDescription));
  }

  public static void createFixedBlockViolation(Oracle o, Train t1, Train t2,
      Connectable connectable) {
    String connectableName = "Unknown";
    if (connectable instanceof Track) {
      connectableName = "Track-" + o.getWorld().getTrackID((Track) connectable);
    } else if (connectable instanceof Switch) {
      connectableName = "Switch-" + o.getWorld().getSwitchID((Switch) connectable);
    }

    String violationDescription = String
        .format("Train %s and Train %s were on the same Connectable %s",
            o.getWorld().getTrainID(t1), o.getWorld().getTrainID(t2), connectableName);

    o.addViolation(new Violation(FIXED_BLOCK, CRITICAL, o.getTick(), violationDescription));
  }

  public static void createOverfullStationViolation(Oracle o, Station station) {
    int stationID = o.getWorld().getStationID(station);
    String violationDescription = String
        .format("Station %s is over capacity (%s out of %s)",
            stationID, station.usedCapacity(), station.getCapacity());

    o.addViolation(new Violation(OVERFULL_STATION, CRITICAL, o.getTick(), violationDescription));
  }
}
