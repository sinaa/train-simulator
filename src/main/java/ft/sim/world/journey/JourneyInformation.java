package ft.sim.world.journey;

import ft.sim.world.WorldHandler;
import ft.sim.world.connectables.Connectable;
import ft.sim.world.connectables.Station;
import ft.sim.world.connectables.Switch;
import ft.sim.world.connectables.Track;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    //this.path = connectableToString(path.getPath());
    this.path = path.getPath().stream().map(Object::toString).collect(Collectors.toList());
  }

  private void setOccupied(JourneyPosition jp){
    //this.occupied = connectableToString(jp.getConnectablesOccupied());
    this.occupied = jp.getConnectablesOccupied().stream().map(Object::toString).collect(Collectors.toList());
  }

  private List<String> connectableToString(List<Connectable> connectables){
    List<String> p = new ArrayList<>();
    for(Connectable c:connectables){
      int id = 0;
      String type = "N/A";
      if(c instanceof Track) {
        id = WorldHandler.getInstance().getWorld().getTrackID((Track) c);
        type = "Track";
      }
      else if(c instanceof Switch) {
        id = WorldHandler.getInstance().getWorld().getSwitchID((Switch) c);
        type = "Switch";
      }
      else if(c instanceof Station) {
        id = WorldHandler.getInstance().getWorld().getStationID((Station) c);
        type = "Station";
      }
      p.add(type + "-" + id);
    }
    return p;
  }
}
