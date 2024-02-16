package edu.brown.cs.student.servertests;

import static org.junit.jupiter.api.Assertions.*;

import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.server.csvrequests.LoadCsvHandler;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import okio.Buffer;
import org.junit.jupiter.api.*;
import org.testng.annotations.BeforeClass;
import spark.Spark;

public class TestLoadCsvHandler {

  @BeforeClass
  public static void setup_before_everything() {
    Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING); // empty name = root logger
  }

  final Map<String, List<List<String>>> csvFile = new HashMap<>();
  private String testFile = System.getProperty("user.dir") + "/data/census/ri_city.csv";

  @BeforeEach
  public void setup() {
    this.csvFile.clear();
    Spark.get("loadcsv", new LoadCsvHandler(csvFile));
    Spark.init();
    Spark.awaitInitialization();
  }

  @AfterEach
  public void teardown() {
    Spark.unmap("loadcsv");
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

  @Test
  public void testLoadFile() throws IOException {
    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=" + this.testFile);
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    LoadCsvHandler.FileSuccessResponse response =
        moshi
            .adapter(LoadCsvHandler.FileSuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    assert response != null;
    assertEquals(response.response_type(), "Your file was loaded successfully!");
    clientConnection.disconnect();
  }

  @Test
  public void testLoadEmptyFile() throws IOException {
    String testFile = "/Users/christinapeng/server-hnguy116-jpeng29/data/numbers/empty.csv";
    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=" + testFile);
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    LoadCsvHandler.FileSuccessResponse response =
        moshi
            .adapter(LoadCsvHandler.FileSuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    assert response != null;
    assertEquals(response.response_type(), "Your file was loaded successfully!");

    clientConnection.disconnect();
  }

  @Test
  public void testLoadRestrictedFile() {
    String restrictedReadMe =
        "/Users/christinapeng/server-hnguy116-jpeng29/src/test/java/edu/brown/cs/student/README";
    HttpURLConnection clientConnection = null;
    try {
      clientConnection = tryRequest("loadcsv?filepath=" + restrictedReadMe);
      assertEquals(403, clientConnection.getResponseCode());
      Moshi moshi = new Moshi.Builder().build();

      LoadCsvHandler.FileSecurityFailureResponse response =
          moshi
              .adapter(LoadCsvHandler.FileSecurityFailureResponse.class)
              .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

      assert response != null;
      assertEquals(response.response_type(), "Your file was loaded successfully!");
    } catch (IOException e) {
      assertTrue(e.getMessage().contains("403"));
    } finally {
      if (clientConnection != null) {
        clientConnection.disconnect();
      }
    }
  }

  @Test
  public void testLoadBadRequest() {
    HttpURLConnection clientConnection = null;
    try {
      clientConnection = tryRequest("loadcsv?filepath=");
      assertEquals(400, clientConnection.getResponseCode());

    } catch (IOException e) {
      assertTrue(e.getMessage().contains("400"));
    } finally {
      if (clientConnection != null) {
        clientConnection.disconnect();
      }
    }
  }

  @Test
  public void testLoadNonexistentFile() {
    HttpURLConnection clientConnection = null;
    try {
      clientConnection = tryRequest("loadcsv?filepath=???");
      assertEquals(404, clientConnection.getResponseCode());

    } catch (IOException e) {
      assertTrue(e.getMessage().contains("404"));
    } finally {
      if (clientConnection != null) {
        clientConnection.disconnect();
      }
    }
  }
}
