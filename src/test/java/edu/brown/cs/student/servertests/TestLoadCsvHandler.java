package edu.brown.cs.student.servertests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.server.csvrequests.LoadCsvHandler;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

public class TestLoadCsvHandler {

  @BeforeAll
  public static void setup_before_everything() {
    Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING); // empty name = root logger
  }

  /**
   * Shared state for all tests. We need to be able to mutate it (adding recipes etc.) but never
   * need to replace the reference itself. We clear this state out after every test runs.
   */
  final Map<String, List<List<String>>> csvFile = new HashMap<>();

  private String testFile = System.getProperty("user.dir") + "data/census/income_by_race.csv";

  /*private String testFile =
  System.getProperty("user.dir")
      + "/data/census/RI City & Town Income from American Community Survey 5-Year Estimates "
      + "Source_ US Census Bureau, 2017-2021 American Community Survey 5-Year Estimates "
      + "2017-2021 - Sheet1.csv";*/

  @BeforeEach
  public void setup() {
    // Re-initialize state, etc. for _every_ test method run
    this.csvFile.clear();

    // In fact, restart the entire Spark server for every test!
    Spark.get("loadcsv", new LoadCsvHandler(csvFile));
    // Spark.get("activity", new ActivityHandler());
    Spark.init();
    Spark.awaitInitialization();
  }

  @AfterEach
  public void teardown() {
    Spark.unmap("loadcsv");
    // Spark.unmap("activity");
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
  private static HttpURLConnection tryRequest(String apiCall) throws IOException {
    // Configure the connection (but don't actually send the request yet)
    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();

    // The default method is "GET", which is what we're using here.
    // If we were using "POST", we'd need to say so.
    clientConnection.setRequestMethod("GET");

    clientConnection.connect();
    return clientConnection;
  }

  @Test
  // Recall that the "throws IOException" doesn't signify anything but acknowledgement to the type
  // checker
  public void testLoadFile() throws IOException {
    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=" + this.testFile);
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    LoadCsvHandler.FileSuccessResponse response =
        moshi
            .adapter(LoadCsvHandler.FileSuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    System.out.println(response);
    clientConnection.disconnect();
  }

  //  @Test
  ////   Recall that the "throws IOException" doesn't signify anything but acknowledgement to the
  // type
  ////   checker
  //  public void testAPIOneRecipe() throws IOException {
  //    menu.add(
  //        Soup.buildNoExceptions(
  //            "Carrot",
  //            Arrays.asList("carrot", "onion", "celery", "garlic", "ginger", "vegetable
  //                broth")));
  //
  //                HttpURLConnection clientConnection = tryRequest("order");
  //    // Get an OK response (the *connection* worked, the *API* provides an error response)
  //    assertEquals(200, clientConnection.getResponseCode());
  //
  //    // Now we need to see whether we've got the expected Json response.
  //    // SoupAPIUtilities handles ingredient lists, but that's not what we've got here.
  //    // NOTE:   (How could we reduce the code repetition?)
  //    Moshi moshi = new Moshi.Builder().build();
  //
  //    // We'll use okio's Buffer class here
  //    System.out.println(clientConnection.getInputStream());
  //    OrderHandler.SoupSuccessResponse response =
  //        moshi
  //            .adapter(OrderHandler.SoupSuccessResponse.class)
  //            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
  //
  //    Soup carrot =
  //        new Soup(
  //            "Carrot",
  //            Arrays.asList("carrot", "onion", "celery", "garlic", "ginger", "vegetable broth"),
  //            false);
  //    Map<String, Object> result = (Map<String, Object>) response.responseMap().get("Carrot");
  //    System.out.println(result.get("ingredients"));
  //    assertEquals(carrot.getIngredients(), result.get("ingredients"));
  //    clientConnection.disconnect();
  //  }
}
