package edu.brown.cs.student.main.server.csvrequests;

import com.squareup.moshi.*;
import edu.brown.cs.student.main.server.csvrequests.LoadCsvHandler.BadJsonFailureResponse;
import edu.brown.cs.student.main.server.csvrequests.LoadCsvHandler.FileNotFoundFailureResponse;
import java.util.*;
import spark.*;

/** If CSV was loaded, serializes it to user. */
public class ViewCsvHandler implements Route {

  private final Map<String, List<List<String>>> csvFile;

  /**
   * Constructor for ViewCsvHandler
   *
   * @param loadedCsv parsed file
   */
  public ViewCsvHandler(Map<String, List<List<String>>> loadedCsv) {
    this.csvFile = loadedCsv;
  }

  /**
   * Returns a loaded CSV or a detailed error message to user.
   *
   * @return serialized response to user
   */
  @Override
  public Object handle(Request request, Response response) {
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
        response.status(200);
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

  /** Response object to send if the file is loaded and viewable. */
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

  /** Response object to send if there is no CSV to view. */
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
