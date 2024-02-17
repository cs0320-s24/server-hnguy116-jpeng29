package edu.brown.cs.student.servertests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static spark.Spark.after;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.Server;
import edu.brown.cs.student.main.server.acsAPI.AcsHandler;
import edu.brown.cs.student.main.server.csvrequests.LoadCsvHandler;
import edu.brown.cs.student.main.server.csvrequests.SearchCsvHandler;
import edu.brown.cs.student.main.server.csvrequests.ViewCsvHandler;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

/**
 * Tests server connection based on different queries
 */
public class ServerTest {

    @BeforeAll
    public static void setupOnce() {
      Spark.port(5555);
      Logger.getLogger("").setLevel(Level.WARNING);
    }

    private final Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    private JsonAdapter<Map<String, Object>> adapter;
    private JsonAdapter<Server> serverJsonAdapter;

    @BeforeEach
    public void setup() {
      final Map<String, List<List<String>>> loadedCsv = new HashMap<>();

      after(
          (request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "*");
          });

      Spark.get("loadcsv", new LoadCsvHandler(loadedCsv));
      try {
        Spark.get("viewcsv", new ViewCsvHandler(loadedCsv));
        Spark.get("searchcsv", new SearchCsvHandler(loadedCsv));
      } catch (NullPointerException e) {
      }
      Spark.get("broadband", new AcsHandler());

      Spark.init();
      Spark.awaitInitialization();
    }

    public void tearDown(String path) {
      // Gracefully stop Spark listening on both endpoints
      Spark.unmap(path);
      Spark.awaitStop(); // don't proceed until the server is stopped
    }

    /**
     * Helper to start a connection to a specific API endpoint/params
     */
    private HttpURLConnection tryRequest(String apiCall) throws IOException {
      URL requestURL = new URL("http://localhost:"+Spark.port()+"/"+apiCall);
      HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
      clientConnection.setRequestProperty("Content-Type", "application/json");
      clientConnection.setRequestProperty("Accept", "application/json");

      clientConnection.connect();
      return clientConnection;
    }

  /**
   * Tests that the LoadCSV can be queried successfully
   * @throws IOException
   */
  @Test
    public void testLoadCSVRequestSuccess() throws IOException {
      HttpURLConnection clientConnection =
          tryRequest(
              "loadcsv?filepath=/Users/havi/Desktop"
                  + "/cs0320/server-hnguy116-jpeng29/data/census/dol_ri_earnings_disparity.csv");
      assertEquals(200, clientConnection.getResponseCode());
      Moshi moshi = new Moshi.Builder().build();
      Server.FileNotLoadedFailureResponse response =
          moshi
              .adapter(Server.FileNotLoadedFailureResponse.class)
              .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
      assertEquals(response.response_type(), "Your file was loaded successfully!");
      clientConnection.disconnect();
      this.tearDown("loadcsv");
    }

  /**
   * Tests that the ViewCSV can be queried successfully
   * @throws IOException
   */
  @Test
  public void testViewCSVRequestSuccess() throws IOException {
    this.testLoadCSVRequestSuccess();
    HttpURLConnection clientConnection =
        tryRequest(
            "viewcsv");
    assertEquals(200, clientConnection.getResponseCode());
    Moshi moshi = new Moshi.Builder().build();
    Server.FileNotLoadedFailureResponse response =
        moshi
            .adapter(Server.FileNotLoadedFailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals(response.response_type(), "success");
    clientConnection.disconnect();
    this.tearDown("viewcsv");
  }

  /**
   * Tests that the SearchCSV can be queried successfully
   * @throws IOException
   */
  @Test
  public void testSearchCSVRequestSuccess() throws IOException {
    this.testLoadCSVRequestSuccess();
    HttpURLConnection clientConnection =
        tryRequest(
            "searchcsv?target=RI");
    assertEquals(200, clientConnection.getResponseCode());
    Moshi moshi = new Moshi.Builder().build();
    Server.FileNotLoadedFailureResponse response =
        moshi
            .adapter(Server.FileNotLoadedFailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals(response.response_type(), "success");
    clientConnection.disconnect();
    this.tearDown("searchcsv?target=RI");
  }

  /**
   * Tests that the AcsHandler can be queried successfully
   * @throws IOException
   */
  @Test
  public void testAcsHandler() throws IOException {
    HttpURLConnection clientConnection =
        tryRequest(
            "broadband?state=California&county="
                + "Santa%20Cruz%20County");
    assertEquals(200, clientConnection.getResponseCode());
    Moshi moshi = new Moshi.Builder().build();
    AcsHandler.BroadbandFailureResponse response =
        moshi
            .adapter(AcsHandler.BroadbandFailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals(response.response_type(), "success");
    clientConnection.disconnect();
  }
}
