package edu.brown.cs.student.servertests;

import static edu.brown.cs.student.servertests.TestLoadCsvHandler.tryRequest;
import static org.junit.jupiter.api.Assertions.*;

import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.server.csvrequests.ViewCsvHandler;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import okio.Buffer;
import org.junit.jupiter.api.*;
import org.testng.annotations.BeforeClass;
import spark.Spark;

/**
 * Tests View Csv Handler
 */
public class TestViewCsvHandler {

  @BeforeClass
  public static void setup_before_everything() {
    Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING); // empty name = root logger
  }

  final Map<String, List<List<String>>> csvFile = new HashMap<>();

  private final String testFile =
      System.getProperty("user.dir") + "/data/census/income_by_race.csv";

  @BeforeEach
  public void setup() {
    this.csvFile.clear();
  }

  @AfterEach
  public void teardown() {
    Spark.unmap("viewcsv");
    Spark.awaitStop();
  }

  /**
   * Helper to start a connection to a specific API endpoint/params
   *
   * @param apiCall the call string, including endpoint (NOTE: this would be better if it had more
   *     structure!)
   * @return the connection for the given URL, just after connecting
   * @throws IOException if the connection fails for some reason
   */
  public static HttpURLConnection tryRequest(String apiCall) throws IOException {
    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();

    clientConnection.setRequestMethod("GET");

    clientConnection.connect();
    return clientConnection;
  }

  /**
   * Tests that the file is viewable
   * @throws IOException
   */
  @Test
  public void testViewFile() throws IOException {
    List<List<String>> nestedList = new ArrayList<>();
    nestedList.add(Arrays.asList("a", "b", "c"));
    nestedList.add(Arrays.asList("d", "e", "f"));
    this.csvFile.put(this.testFile, nestedList);
    Spark.get("viewcsv", new ViewCsvHandler(csvFile));
    Spark.init();
    Spark.awaitInitialization();
    HttpURLConnection clientConnection = tryRequest("viewcsv");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    ViewCsvHandler.FileSuccessResponse response =
        moshi
            .adapter(ViewCsvHandler.FileSuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    assert response != null;
    assertEquals(response.response_type(), "success");
    if (response.responseMap().containsKey(this.testFile)) {
      Object retrievedObject = response.responseMap().get(this.testFile);
      if (retrievedObject instanceof List) {
        List<List<String>> retrievedList = (List<List<String>>) retrievedObject;
        assertEquals(retrievedList.get(0).get(0), "a");
        assertEquals(retrievedList.get(0).get(1), "b");
        assertEquals(retrievedList.get(0).get(2), "c");
        assertEquals(retrievedList.get(1).get(0), "d");
        assertEquals(retrievedList.get(1).get(1), "e");
        assertEquals(retrievedList.get(1).get(2), "f");
      }
    }
    clientConnection.disconnect();
  }

  /**
   * Tests when a user attempts to view an empty file
   * @throws IOException
   */
  @Test
  public void testViewEmptyFile() throws IOException {
    this.csvFile.put(this.testFile, new ArrayList<>());
    Spark.get("viewcsv", new ViewCsvHandler(csvFile));
    Spark.init();
    Spark.awaitInitialization();
    HttpURLConnection clientConnection = tryRequest("viewcsv");
    assertEquals(204, clientConnection.getResponseCode());
    clientConnection.disconnect();
  }

  /**
   * Tests when a user attempts to view a null file
   * @throws IOException
   */
  @Test
  public void testViewNullFile() throws IOException {
    Spark.get("viewcsv", new ViewCsvHandler(null));
    Spark.init();
    Spark.awaitInitialization();
    HttpURLConnection clientConnection = tryRequest("viewcsv");
    assertEquals(500, clientConnection.getResponseCode());
    clientConnection.disconnect();
  }
}
