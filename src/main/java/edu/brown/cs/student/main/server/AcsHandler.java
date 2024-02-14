package edu.brown.cs.student.main.server;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.io.IOException;
import java.net.*;
import java.net.http.*;
import java.util.*;
import okio.Buffer;
import spark.*;

// retrieve the percentage of households with broadband access for a target location
// //by providing the name of the target state and county in my request

// the response should include the date and time that all data was retrieved from the ACS API by
// your API server, as well as the state and county names your server received.

public class AcsHandler implements Route {

  private StateCodes myStateCodeMap;

  @Override
  public Object handle(Request request, Response response) throws DatasourceException {
    try {
      String county = request.queryParams("county");
      String state = request.queryParams("state");
      this.myStateCodeMap = this.retrieveStateCodes();
      // Integer stateCode = this.myStateCodeMap.states.get(state);
      // System.out.println(stateCode);

      // System.out.println(this.retrieveStateCodes());

      // get state codes once from file
      // get state + county from user
      // find state code from state
      // new requestURL for county:
      // https://api.census.gov/data/2010/dec/sf1?get=NAME&for=county:*&in=state:06
      // new requestURL for percents
      // return percent of households w broadband access
      // return date and time

      //    Map<String, Object> responseMap = new HashMap<>();
      //    try {
      //      // Sends a request to the API and receives JSON back
      //      //String acsJson = this.sendRequest();
      //
      // //api.census.gov/data/2022/acs/acs1?get=NAME,group(B01001)&for=us:1&key=YOUR_KEY_GOES_HERE
      //      URL requestURL = new
      // URL("https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*");
      //      HttpURLConnection clientConnection = connect(requestURL);
      //      Moshi moshi = new Moshi.Builder().build();
      //
      //      JsonAdapter<StateCodes> adapter = moshi.adapter(StateCodes.class).nonNull();
      //      // NOTE: important! pattern for handling the input stream
      //      StateCodes body = adapter.fromJson(new
      // Buffer().readFrom(clientConnection.getInputStream()));
      //      clientConnection.disconnect();
      //      // Deserializes JSON into an Activity
      //      // Activity activity = ActivityAPIUtilities.deserializeActivity(activityJson);
      //      // Adds results to the responseMap
      //      responseMap.put("result", "success");
      //      // responseMap.put("acs", activity);
      //      return responseMap;
      //    } catch (Exception e) {
      //      e.printStackTrace();
      //      // This is a relatively unhelpful exception message. An important part of this sprint
      // will be
      //      // in learning to debug correctly by creating your own informative error messages
      // where
      // Spark
      //      // falls short.
      //      responseMap.put("result", "Exception");
      //    }
      Map<String, Object> responseMap = new HashMap<>();
      responseMap.put("hi", this.myStateCodeMap.states);
      System.out.println(this.myStateCodeMap.states);
      System.out.println(this.myStateCodeMap.states());
      return new StateCodeSuccessResponse().serialize();
    } catch (Exception e) {
      return new DatasourceException("error" + e);
    }
  }

  private static StateCodes retrieveStateCodes() throws DatasourceException {
    try {
      URL requestURL = new URL("https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*");
      HttpURLConnection clientConnection = connect(requestURL);
      Moshi moshi = new Moshi.Builder().build();

      // NOTE WELL: THE TYPES GIVEN HERE WOULD VARY ANYTIME THE RESPONSE TYPE VARIES
      JsonAdapter<StateCodes> adapter = moshi.adapter(StateCodes.class).nonNull();
      // NOTE: important! pattern for handling the input stream
      StateCodes body = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
      clientConnection.disconnect();
      if (body == null || body.states() == null) {
        throw new DatasourceException("Malformed response from NWS");
      }
      return body;
    } catch (IOException e) {
      throw new DatasourceException(e.getMessage());
    }
  }

  private static HttpURLConnection connect(URL requestURL) throws DatasourceException, IOException {
    URLConnection urlConnection = requestURL.openConnection();
    if (!(urlConnection instanceof HttpURLConnection))
      throw new DatasourceException("unexpected: result of connection wasn't HTTP");
    HttpURLConnection clientConnection = (HttpURLConnection) urlConnection;
    clientConnection.connect(); // GET
    if (clientConnection.getResponseCode() != 200)
      throw new DatasourceException(
          "unexpected: API connection not success status " + clientConnection.getResponseMessage());
    return clientConnection;
  }

  public record StateCodes(List<List<String>> states) {}
  // Note: case matters! "gridID" will get populated with null, because "gridID" != "gridId"
  // public record StateCodesProperties(String gridId, String gridX, String gridY, String timeZone,
  // String radarStation) {}

  public record ForecastResponse(String id, ForecastResponseProperties properties) {}

  public record ForecastResponseProperties(
      String updateTime, ForecastResponseTemperature temperature) {}

  public record ForecastResponseTemperature(String uom, List<ForecastResponseTempValue> values) {}

  public record ForecastResponseTempValue(String validTime, double value) {}

  public record StateCodeSuccessResponse(String response_type) {
    public StateCodeSuccessResponse() {
      this("Your file was loaded successfully!");
    }
    /**
     * @return this response, serialized as Json
     */
    String serialize() {
      try {
        // Initialize Moshi which takes in this class and returns it as JSON!
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<AcsHandler.StateCodeSuccessResponse> adapter =
            moshi.adapter(AcsHandler.StateCodeSuccessResponse.class);
        return adapter.toJson(this);
      } catch (Exception e) {
        // For debugging purposes, show in the console _why_ this fails
        // Otherwise we'll just get an error 500 from the API in integration
        // testing.
        e.printStackTrace();
        throw e;
      }
    }
  }
}
