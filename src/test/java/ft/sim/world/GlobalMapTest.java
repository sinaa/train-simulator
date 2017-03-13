package ft.sim.world;

import static org.junit.Assert.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

/**
 * Created by Sina on 13/03/2017.
 */
public class GlobalMapTest {

  @Test
  public void testOriginalMap(){
    Gson gson = new Gson();
    //Gson gson = new GsonBuilder().setPrettyPrinting().create();

    OriginalMapBuilder world = new OriginalMapBuilder();
    String j = gson.toJson(world);
    System.out.println(j);
  }

  @Test
  public void testGlobalMap(){
    GlobalMap gm = new GlobalMap();
    Gson gson = new Gson();
    //Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String json = gson.toJson(gm);
    System.out.println(json);
  }

}