package edu.brown.cs.student.main.server.csvrequests;

import com.squareup.moshi.*;
import java.util.*;
import spark.*;

public class ViewCsvHandler implements Route {

  private final Map<String, List<List<String>>> csvFile;

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
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<FileSuccessResponse> adapter = moshi.adapter(FileSuccessResponse.class);
        return adapter.toJson(this);
      } catch (Exception e) {
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
