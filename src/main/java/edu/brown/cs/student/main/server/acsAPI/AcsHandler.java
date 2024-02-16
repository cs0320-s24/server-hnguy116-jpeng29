package edu.brown.cs.student.main.server.acsAPI;

import com.squareup.moshi.*;
import java.io.IOException;
import java.net.*;
import java.util.*;
import spark.*;

public class AcsHandler implements Route {

  @Override
  public Object handle(Request request, Response response) throws DatasourceException {
    try {
      String county = request.queryParams("county");
      String state = request.queryParams("state");

      Map<String, String> myStateCodeMap = retrieveStateCodes();
      String stateCode = myStateCodeMap.get(state);
      Map<String, String> myCountyCodeMap = retrieveCountyCodes(myStateCodeMap.get(state));
      String countyCode = myCountyCodeMap.get(county + ", " + state);

      URL requestURL =
          new URL(
              "https://api.census.gov/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&for=county:"
                  + countyCode
                  + "&in=state:"
                  + stateCode);

      Cache cache = new Cache(Specification.SIZE, 100);

      List<List<String>> body = cache.get(requestURL);

      Map<String, Object> responseMap = new HashMap<>();
      for (int i = 1; i < body.size(); i++) {
        if (body.get(i).size() >= 4) {
          responseMap.put("Broadband Access Percentage", body.get(i).get(1));
        }
      }
      String retrievalDateTime = java.time.LocalDateTime.now().toString();
      responseMap.put("Retrieval Date & Time", retrievalDateTime);

      // Add the state and county names
      responseMap.put("state", state);
      responseMap.put("county", county);

      return new BroadbandSuccessResponse(responseMap).serialize();
    } catch (IOException e) {
      throw new DatasourceException(e.getMessage());
    } catch (Exception e) {
      return new DatasourceException("error" + e);
    }
  }

  private static Map<String, String> retrieveStateCodes() throws DatasourceException {
    try {
      URL requestURL = new URL("https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*");
      List<List<String>> body = Cache.retrieveJson(requestURL);
      Map<String, String> stateCodeMap = new HashMap<>();
      for (List<String> state : body) {
        if (state.size() >= 2) {
          stateCodeMap.put(state.get(0), state.get(1));
        }
      }
      return stateCodeMap;
    } catch (IOException e) {
      throw new DatasourceException(e.getMessage());
    }
  }

  private static Map<String, String> retrieveCountyCodes(String stateCode)
      throws DatasourceException {
    try {
      URL requestURL =
          new URL(
              "https://api.census.gov/data/2010/dec/sf1?get=NAME&for=county:*&in=state:"
                  + stateCode);
      List<List<String>> body = Cache.retrieveJson(requestURL);
      Map<String, String> countyCodeMap = new HashMap<>();
      for (List<String> county : body) {
        if (county.size() >= 3) {
          countyCodeMap.put(county.get(0), county.get(2));
        }
      }
      return countyCodeMap;
    } catch (IOException e) {
      throw new DatasourceException(e.getMessage());
    }
  }

  //  public record StateCodes(List<List<String>> states) {}
  //
  //  public record ForecastResponse(String id, ForecastResponseProperties properties) {}
  //
  //  public record ForecastResponseProperties(
  //      String updateTime, ForecastResponseTemperature temperature) {}
  //
  //  public record ForecastResponseTemperature(String uom, List<ForecastResponseTempValue> values)
  // {}
  //
  //  public record ForecastResponseTempValue(String validTime, double value) {}

  public record BroadbandSuccessResponse(String response_type, Map<String, Object> responseMap) {
    public BroadbandSuccessResponse(Map<String, Object> responseMap) {
      this("success", responseMap);
    }
    /**
     * @return this response, serialized as Json
     */
    String serialize() {
      try {
        // Initialize Moshi which takes in this class and returns it as JSON!
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<AcsHandler.BroadbandSuccessResponse> adapter =
            moshi.adapter(AcsHandler.BroadbandSuccessResponse.class);
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
