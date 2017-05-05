package ft.sim.world.map;

import com.google.common.collect.Lists;
import ft.sim.world.connectables.Section;
import ft.sim.world.connectables.Track;
import ft.sim.world.placeables.ActiveBalise;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by sina on 02/05/2017.
 */
public class DualLineHelper {

  public static void pairAllTracks(List<Track> tracks, List<Track> otherTracks) {
    //List<Track> otherTracks = Lists.reverse(opposite);

    if (tracks.size() != otherTracks.size()) {
      throw new IllegalStateException("Equal number of tracks must be paired. "
          + tracks.size() + " != " + otherTracks.size());
    }

    for (int i = 0; i < tracks.size(); i++) {
      Track t1 = tracks.get(i);
      Track t2 = otherTracks.get(i);

      pairTracks(t1, t2);
    }
  }

  public static void pairTracks(Track t1, Track t2) {
    if (t1.getLength() != t2.getLength()) {
      throw new IllegalStateException("Tracks of different length detected." +
          " T1: " + t1.getLength() + ", T2: " + t2.getLength());
    }

    List<Section> sections1 = t1.getSections();
    List<Section> sections2 = Lists.reverse(t2.getSections());
    for (int j = 0; j < sections1.size(); j++) {
      ActiveBalise ab1 = getActiveBalise(sections1.get(j));
      ActiveBalise ab2 = getActiveBalise(sections2.get(j));

      if (ab1 == null && ab2 == null) {
        continue;
      }
      // sanity checks
      if (ab1 == null) {
        throw new IllegalStateException("Null balise on left side, Non-null on right side");
      } else if (ab2 == null) {
        throw new IllegalStateException("Null balise on right side, Non-null on left side");
      }

      // pair the two balises
      ab1.setDualTrackPair(ab2);
      ab2.setDualTrackPair(ab1);
    }
  }

  private static ActiveBalise getActiveBalise(Section section) {
    return section.getPlaceables().stream().filter(p -> p instanceof ActiveBalise)
        .map(p -> (ActiveBalise) p).map(Optional::ofNullable).findFirst()
        .flatMap(Function.identity()).orElse(null);
  }

}
