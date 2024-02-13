package edu.brown.cs.student.main.server;

import com.squareup.moshi.*;
import java.util.*;
import spark.*;

public class ViewCsvHandler implements Route {

  private Map<String, List<List<String>>> csvFile;

  public ViewCsvHandler(Map<String, List<List<String>>> loadedCsv) {
    this.csvFile = loadedCsv;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    try {
      Map<String, Object> responseMap = new HashMap<>();
      if (!this.csvFile.isEmpty()) {
        String firstKey = this.csvFile.keySet().iterator().next();
        responseMap.put(firstKey, this.csvFile.get(firstKey));
      }

      if (!responseMap.isEmpty()) {
        return new FileSuccessResponse(responseMap).serialize();
      } else {
        System.out.println("oh no");
        return new SoupNoRecipesFailureResponse().serialize();
      }
    } catch (Exception e) {
      System.out.println("error" + e);
      return new SoupNoRecipesFailureResponse().serialize();
    }
  }

  /** Response object to send, containing a soup with certain ingredients in it */
  public record FileSuccessResponse(String response_type, Map<String, Object> responseMap) {
    public FileSuccessResponse(Map<String, Object> responseMap) {
      this("success", responseMap);
    }
    /**
     * @return this response, serialized as Json
     */
    String serialize() {
      try {
        // Initialize Moshi which takes in this class and returns it as JSON!
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<FileSuccessResponse> adapter = moshi.adapter(FileSuccessResponse.class);
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

  /** Response object to send if someone requested soup from an empty Menu */
  public record SoupNoRecipesFailureResponse(String response_type) {
    public SoupNoRecipesFailureResponse() {
      this("error");
    }

    /**
     * @return this response, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(SoupNoRecipesFailureResponse.class).toJson(this);
    }
  }
}
