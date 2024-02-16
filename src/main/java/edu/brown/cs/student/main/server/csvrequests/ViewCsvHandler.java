package edu.brown.cs.student.main.server.csvrequests;

import com.squareup.moshi.*;
import edu.brown.cs.student.main.server.csvrequests.LoadCsvHandler.BadJsonFailureResponse;
import edu.brown.cs.student.main.server.csvrequests.LoadCsvHandler.FileNotFoundFailureResponse;
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
        List<List<String>> firstValue = this.csvFile.get(firstKey);
        if (!firstValue.isEmpty()) {
          responseMap.put(firstKey, this.csvFile.get(firstKey));
        }
      }
      if (!responseMap.isEmpty()) {
        return new FileSuccessResponse(responseMap).serialize();
      } else {
        System.out.println("nothing to view");
        response.status(204);
        return new NoCsvFailureResponse().serialize();
      }
    } catch (Exception e) {
      System.out.println("error" + e);
      response.status(500);
      return new FileNotFoundFailureResponse().serialize();
    }
  }

  public record FileSuccessResponse(String response_type, Map<String, Object> responseMap) {
    public FileSuccessResponse(Map<String, Object> responseMap) {
      this("success", responseMap);
    }

    String serialize() {
      try {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<FileSuccessResponse> adapter = moshi.adapter(FileSuccessResponse.class);
        return adapter.toJson(this);
      } catch (JsonDataException e) {
        return new BadJsonFailureResponse().serialize();
      } catch (Exception e) {
        e.printStackTrace();
        throw e;
      }
    }
  }

  public record NoCsvFailureResponse(String response_type) {
    public NoCsvFailureResponse() {
      this("error_datasource");
    }

    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(NoCsvFailureResponse.class).toJson(this);
    }
  }
}
