package ft.sim.world;

import ft.sim.simulation.BasicSimulation;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sina on 06/03/2017.
 */
public class JourneyInformation {

  List<String> path = new ArrayList<>();
  List<String> occupied = new ArrayList<>();

  public void update(Journey j){
    setPath(j.getJourneyPath());
    setOccupied(j.getJourneyPosition());
  }

  private void setPath(JourneyPath path) {
    this.path = connectableToString(path.getPath());
  }

  private void setOccupied(JourneyPosition jp){
    this.occupied = connectableToString(jp.getConnectablesOccupied());
  }

  private List<String> connectableToString(List<Connectable> connectables){
    List<String> p = new ArrayList<>();
    for(Connectable c:connectables){
      int id = 0;
      String type = "N/A";
      if(c instanceof Track) {
        id = BasicSimulation.world.getTrackID((Track) c);
        type = "Track";
      }
      if(c instanceof Switch) {
        id = BasicSimulation.world.getSwitchID((Switch) c);
        type = "Switch";
      }
      p.add(type + "-" + id);
    }
    return p;
  }
}
