package ft.sim.world;

import com.google.gson.Gson;

import ft.sim.simulation.SimulationController;
import ft.sim.world.map.MapBuilderHelper;
import org.junit.Test;

import java.util.List;
import ft.sim.world.map.GlobalMap;
import ft.sim.world.map.MapBuilder;
import ft.sim.world.map.OriginalMapBuilder;

/**
 * Created by Sina on 13/03/2017.
 */
public class GlobalMapTest {

  @Test
  public void testOriginalMap() {
    Gson gson = new Gson();

    OriginalMapBuilder world = new OriginalMapBuilder();
    String j = gson.toJson(world);
    //System.out.println(j);
  }

  @Test
  public void testGlobalMap() {
    GlobalMap gm = MapBuilder.buildNewMap(SimulationController.DEFAULT_MAP);
    WorldHandler.getInstance(gm);
    Gson gson = new Gson();
    String json = gson.toJson(gm);
    //System.out.println(json);
  }

  @Test
  public void testMapResolver() {
    List<String> maps = MapBuilderHelper.getMaps();
    //System.out.println(maps);
  }

}