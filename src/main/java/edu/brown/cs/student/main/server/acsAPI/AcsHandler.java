package edu.brown.cs.student.main.server.acsAPI;

import com.squareup.moshi.*;
import java.io.IOException;
import java.net.*;
import java.util.*;
import spark.*;

/**
 * Handler class for ACS requests. Query gets target location through state and county, using the
 * information to return the percentage of households with broadband access.
 */
public class AcsHandler implements Route {

  /**
   * Retrieves Broadband Access percentage and retrieval data and time
   *
   * @return response object detailing method success/failure and percentage data
   * @throws DatasourceException when the method fails to retrieve
   */
  @Override
  public Object handle(Request request, Response response) throws DatasourceException {
    try {
      String county = request.queryParams("county");
      String state = request.queryParams("state");

      if (county == null || state == null) {
        response.status(400);
        return new RequestFailureResponse().serialize();
      }

      Map<String, String> myStateCodeMap = this.retrieveStateCodes();
      String stateCode = myStateCodeMap.get(state);
      Map<String, String> myCountyCodeMap = this.retrieveCountyCodes(myStateCodeMap.get(state));
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
      response.status(200);
      return new BroadbandSuccessResponse(responseMap).serialize();
    } catch (IOException e) {
      System.out.println("ioexception " + e.getMessage());
      response.status(500);
      return new BroadbandFailureResponse().serialize();
    } catch (DatasourceException e) {
      System.out.println("data source exception " + e.getMessage());
      response.status(404);
      return new DataSourceFailureResponse().serialize();
    }
  }

  /**
   * Retrieves state codes from the ACS API and stores it in a map
   *
   * @return map of states and their codes
   * @throws DatasourceException when method fails to retrieve state codes
   */
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

  /**
   * Retrieves county codes from the ACS API and stores it in a map
   *
   * @param stateCode target state to retrieve codes for
   * @return county codes for the target state
   * @throws DatasourceException when the method fails to retrieve the county codes
   */
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

  /** Response object to send if the broadband call is a success */
  public record BroadbandSuccessResponse(String response_type, Map<String, Object> responseMap) {
    public BroadbandSuccessResponse(Map<String, Object> responseMap) {
      this("success", responseMap);
    }
    /**
     * @return this response, serialized as Json
     */
    private String serialize() {
      try {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<AcsHandler.BroadbandSuccessResponse> adapter =
            moshi.adapter(AcsHandler.BroadbandSuccessResponse.class);
        return adapter.toJson(this);
      } catch (Exception e) {
        e.printStackTrace();
        throw e;
      }
    }
  }

  /** Response object to send if the broadband call runs into an error */
  public record BroadbandFailureResponse(String response_type) {
    public BroadbandFailureResponse() {
      this("error");
    }
    /**
     * @return this response, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(BroadbandFailureResponse.class).toJson(this);
    }
  }

  /** Response object to send if the broadband call runs into an error */
  public record RequestFailureResponse(String response_type) {
    public RequestFailureResponse() {
      this("bad_request");
    }
    /**
     * @return this response, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(RequestFailureResponse.class).toJson(this);
    }
  }

  /** Response object to send if the broadband call runs into a data source error */
  public record DataSourceFailureResponse(String response_type) {
    public DataSourceFailureResponse() {
      this("error_datasource");
    }
    /**
     * @return this response, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(DataSourceFailureResponse.class).toJson(this);
    }
  }
}
