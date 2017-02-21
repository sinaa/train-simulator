package ft.sim.simulation;

import ft.sim.world.Track;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Sina on 21/02/2017.
 */
public class Journey {

  List<Track> tracks;

  public Journey(List<Track> tracks){
      for(Track track:tracks)
        this.tracks.add(track);

      
  }

}
