package edu.brown.cs.student.servertests;

import static org.junit.jupiter.api.Assertions.*;

import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.csv.CsvParser;
import edu.brown.cs.student.main.csv.creatorfromrow.FactoryFailureException;
import edu.brown.cs.student.main.csv.creatorfromrow.ParsedObject;
import edu.brown.cs.student.main.server.csvrequests.SearchCsvHandler;
import java.io.FileReader;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import okio.Buffer;
import org.junit.jupiter.api.*;
import org.testng.annotations.BeforeClass;
import spark.Spark;

/** Class to test Search CSV Handler functionality */
public class TestSearchCsvHandler {

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
    Spark.unmap("searchcsv");
    Spark.awaitStop();
  }

  /**
   * Helper to start a connection to a specific API endpoint/params
   *
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
   * Test file searching
   *
   * @throws IOException
   */
  @Test
  public void testSearchFile() throws IOException, FactoryFailureException {
    FileReader fileReader = new FileReader(testFile);
    ParsedObject MY_PARSED_OBJECT = new ParsedObject();
    CsvParser<List<String>> MY_PARSER = new CsvParser<>(fileReader, MY_PARSED_OBJECT);
    List<List<String>> loadedFile = MY_PARSER.parse();
    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put(testFile, loadedFile);
    this.csvFile.put(testFile, loadedFile);

    Spark.get("searchcsv", new SearchCsvHandler(csvFile));
    Spark.init();
    Spark.awaitInitialization();
    HttpURLConnection clientConnection = tryRequest("searchcsv?target=White");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    SearchCsvHandler.FileSuccessResponse response =
        moshi
            .adapter(SearchCsvHandler.FileSuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assert response != null;

    assertEquals(response.responseMap().values().size(), 40);
    assertTrue(response.responseMap().keySet().contains("242"));
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

  // Search empty file
  @Test
  public void testSearchEmptyFile() throws IOException {
    this.csvFile.put(this.testFile, new ArrayList<>());
    Spark.get("searchcsv", new SearchCsvHandler(csvFile));
    Spark.init();
    Spark.awaitInitialization();
    HttpURLConnection clientConnection = tryRequest("searchcsv");
    assertEquals(400, clientConnection.getResponseCode());
    clientConnection.disconnect();
  }

  // Search null file
  @Test
  public void testSearchNullFile() throws IOException {
    Spark.get("searchcsv", new SearchCsvHandler(null));
    Spark.init();
    Spark.awaitInitialization();
    HttpURLConnection clientConnection = tryRequest("searchcsv");
    assertEquals(400, clientConnection.getResponseCode());
    clientConnection.disconnect();
  }

  // Search w no target
  @Test
  public void testSearchNoVal() throws IOException {
    Spark.get("searchcsv", new SearchCsvHandler(this.csvFile));
    Spark.init();
    Spark.awaitInitialization();
    HttpURLConnection clientConnection = tryRequest("searchcsv?target=");
    assertEquals(400, clientConnection.getResponseCode());
    clientConnection.disconnect();
  }
}
