package ft.sim.world;

import ft.sim.world.connectables.Track;
import ft.sim.world.placeables.PassiveBalise;
import ft.sim.world.placeables.Placeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.Yaml;

/**
 * Created by Sina on 13/03/2017.
 */
public class MapImporterTest {

  @Test
  public void readYaml() {
    Yaml yaml = new Yaml();
    String document = "\n- Hesperiidae\n- Papilionidae\n- Apatelodidae\n- Epiplemidae";
    List<String> list = (List<String>) yaml.load(document);
    //System.out.println(list);
  }

  @Test
  public void readYamlFile() throws IOException {
    Resource resource = new ClassPathResource("maps/basic.yaml");
    Yaml yaml = new Yaml();
    Map<String, Object> list = (Map<String, Object>) yaml.load(resource.getInputStream());

    //System.out.println(list);
  }

  @Test
  public void tracksTest() throws IOException {
    Resource resource = new ClassPathResource("maps/basic.yaml");
    Yaml yaml = new Yaml();
    Map<String, Object> map = (Map<String, Object>) yaml.load(resource.getInputStream());

    Map<String, Object> tracks = (Map<String, Object>) map.get("tracks");
    for (Entry<String, Object> track : tracks.entrySet()) {
      int trackID = Integer.parseInt(track.getKey());
      Map<String, Integer> trackData = (Map<String, Integer>) track.getValue();
      Track t = new Track(trackData.get("numSections"));
      //trackMap.put(trackID, t);
    }

    Map<String, Object> placeables = (Map<String, Object>) map.get("placeables");
    for (Entry<String, Object> placeable : placeables.entrySet()) {
      int placeableID = Integer.parseInt(placeable.getKey());
      Map<String, Object> placeableData = (Map<String, Object>) placeable.getValue();
      Map<String, Integer> placeOnMap = ((Map<String, Integer>) placeableData.get("placeOn"));
      Placeable p;
      if (placeableData.get("type").equals("fixedBalise")) {
        p = new PassiveBalise(Double.valueOf((int) placeableData.get("advisorySpeed")), placeableID);
      }

      int trackID = placeOnMap.get("track");
      int section = placeOnMap.get("section");

      //Track t = new Track(trackData.get("numSections"));
      //trackMap.put(trackID, t);
    }

    Map<String, Object> switches = (Map<String, Object>) map.get("switches");
    for (Entry<String, Object> s: switches.entrySet()) {
      int switchID = Integer.parseInt(s.getKey());
      Map<String, Object> sData = (Map<String, Object>) s.getValue();
      List<Integer> left = (List<Integer>) sData.get("left");
      List<Integer> right = (List<Integer>) sData.get("right");
      int statusLeft = (int) sData.get("statusLeft");
      int statusRight = (int) sData.get("statusRight");
    }

    Map<String, Object> journeyPaths = (Map<String, Object>) map.get("journeyPaths");
    for (Entry<String, Object> journeyPath: journeyPaths.entrySet()) {
      int journeyPathID = Integer.parseInt(journeyPath.getKey());
      Map<String, Object> jData = (Map<String, Object>) journeyPath.getValue();
      List<Map<String, String>> path = (List<Map<String, String>>) jData.get("path");
      for(Map<String, String> connectable: path){
        //System.out.println(connectable);
      }
    }
  }

}
