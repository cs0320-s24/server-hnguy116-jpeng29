 package edu.brown.cs.student.servertests;

 import static org.junit.jupiter.api.Assertions.assertEquals;
 import static org.testng.AssertJUnit.assertTrue;
 import com.squareup.moshi.Moshi;
 import edu.brown.cs.student.main.server.acsAPI.AcsHandler;
 import edu.brown.cs.student.main.server.acsAPI.Cache;
 import edu.brown.cs.student.main.server.acsAPI.DatasourceException;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
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

 /**
  * Class to test ACS Handler functionality.
  */
 public class TestAcsHandler {

    @BeforeAll
    public static void setup_before_everything() {
      Spark.port(5555);
      Logger.getLogger("").setLevel(Level.WARNING);
    }

    /**
     * Shared state for all tests. We need to be able to mutate it but never
     * need to replace the reference itself. We clear this state out after every test runs.
     */
    final List<String> stateCodes = new ArrayList<>();

    @BeforeEach
    public void setup() {
      this.stateCodes.clear();
      Spark.get("broadband", new AcsHandler());
      Spark.init();
      Spark.awaitInitialization();
    }

    @AfterEach
    public void teardown() {
      Spark.unmap("broadband");
      Spark.awaitStop();
    }

    /**
     * Helper to start a connection to a specific API endpoint/params
     *
     * @param apiCall the call string, including endpoint
     * @return the connection for the given URL, just after connecting
     * @throws IOException if the connection fails for some reason
     */
    private static HttpURLConnection tryRequest(String apiCall) throws IOException {
      URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
      HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
      clientConnection.setRequestMethod("GET");
      clientConnection.connect();
      return clientConnection;
    }

   /**
    * Tests Acs Handler with specific county and state query
    * @throws IOException
    */
    @Test
    public void testAcsHandler() throws IOException {
      HttpURLConnection clientConnection = tryRequest("broadband?state=California&county="
          + "Santa%20Cruz%20County"); //create new tryRequest
      // Get an OK response (the *connection* worked, the *API* provides an error response)
      assertEquals(200, clientConnection.getResponseCode());
      Moshi moshi = new Moshi.Builder().build();
      AcsHandler.BroadbandFailureResponse response =
          moshi
              .adapter(AcsHandler.BroadbandFailureResponse.class)
              .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
      assertEquals(response.response_type(), "success");
      clientConnection.disconnect();
    }

   /**
    * Tests Acs Handler with no county parameter
    * @throws IOException
    */
   @Test
   public void testNoCounty() throws IOException {
     HttpURLConnection clientConnection = tryRequest("broadband?state=California");
     try {
       assertEquals(400, clientConnection.getResponseCode());
       Moshi moshi = new Moshi.Builder().build();
       AcsHandler.BroadbandFailureResponse response =
           moshi
               .adapter(AcsHandler.BroadbandFailureResponse.class)
               .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
       assertEquals(response.response_type(), "error");
       clientConnection.disconnect();
     } catch (IOException e){
       assertTrue(e.getMessage().contains("400"));
     }
   }

   /**
    * Tests Acs Handler with no state parameter
    * @throws IOException
    */
   @Test
   public void testNoState() throws IOException {
     HttpURLConnection clientConnection = tryRequest("broadband?county=Santa%20Cruz%20County");
     try {
       assertEquals(400, clientConnection.getResponseCode());
       Moshi moshi = new Moshi.Builder().build();
       AcsHandler.BroadbandFailureResponse response =
           moshi
               .adapter(AcsHandler.BroadbandFailureResponse.class)
               .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
       assertEquals(response.response_type(), "error");
       clientConnection.disconnect();
     } catch (IOException e){
       assertTrue(e.getMessage().contains("400"));
     }
   }

   /**
    * Tests state code retrieval using a specific state query
    */
   @Test
   public void testStateCodes() throws IOException, DatasourceException {
     try {
       URL requestURL = new URL("https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*");
       List<List<String>> body = Cache.retrieveJson(requestURL);
       Map<String, String> stateCodeMap = new HashMap<>();
       for (List<String> state : body) {
         if (state.size() >= 2) {
           stateCodeMap.put(state.get(0), state.get(1));
         }
       }
       assertTrue(!stateCodeMap.isEmpty());
       assertEquals(stateCodeMap.size(), 53);
       assertTrue(stateCodeMap.keySet().size() == 53);
       assertTrue(stateCodeMap.values().size() == 53);
       assertEquals(stateCodeMap.get("Delaware"), "10");
       assertTrue(stateCodeMap.containsKey("Puerto Rico"));
     } catch (IOException | DatasourceException e) {
       throw new DatasourceException(e.getMessage());
     }
   }

   /**
    * Tests county code retrieval using a specific state code query
    */
   @Test
   public void testCountyCodes() throws DatasourceException {
     try {
       URL requestURL =
           new URL(
               "https://api.census.gov/data/2010/dec/sf1?get=NAME&for=county:*&in=state:27");
       List<List<String>> body = Cache.retrieveJson(requestURL);
       Map<String, String> countyCodeMap = new HashMap<>();
       for (List<String> county : body) {
         if (county.size() >= 3) {
           countyCodeMap.put(county.get(0), county.get(2));
         }
       }
       assertTrue(!countyCodeMap.isEmpty());
       assertEquals(countyCodeMap.size(), 88);
       assertTrue(countyCodeMap.keySet().size() == 88);
       assertTrue(countyCodeMap.values().size() == 88);
       assertEquals(countyCodeMap.get("Blue Earth County, Minnesota"), "013");
       assertTrue(countyCodeMap.containsKey("Martin County, Minnesota"));
     } catch (IOException | DatasourceException e) {
       throw new DatasourceException(e.getMessage());
     }
   }
 }