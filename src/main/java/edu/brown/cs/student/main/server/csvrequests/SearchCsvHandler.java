package edu.brown.cs.student.main.server.csvrequests;

import com.squareup.moshi.*;
import edu.brown.cs.student.main.server.csvrequests.LoadCsvHandler.BadJsonFailureResponse;
import edu.brown.cs.student.main.server.csvrequests.LoadCsvHandler.BadRequestFailureResponse;
import java.util.*;
import spark.*;

/** Handles searching functionality for CSV requests */
public class SearchCsvHandler implements Route {

  private Map<String, List<List<String>>> csvFile;

  /**
   * Constructor for the class. Initializes csvFile with the passed in map.
   *
   * @param loadedCsv parsed CSV
   */
  public SearchCsvHandler(Map<String, List<List<String>>> loadedCsv) {
    this.csvFile = loadedCsv;
  }

  /**
   * Searches through a file for a certain value; method is customizable to search with guidance
   * (header name or column index) or to search the entire file.
   *
   * @return response object detailing method success/failure and matched rows
   */
  @Override
  public Object handle(Request request, Response response) {
    try {
      String header = request.queryParams("header");
      String target = request.queryParams("target");
      String columnIndexString = request.queryParams("columnIndex");

      if (this.csvFile == null || target == null || this.csvFile.size() == 0) {
        response.status(400);
        return new BadRequestFailureResponse().serialize();
      }

      Integer columnIndex = null;
      if (columnIndexString != null) {
        columnIndex = Integer.parseInt(columnIndexString);
      }

      Map<String, Object> responseMap = new HashMap<>();
      String firstKey = this.csvFile.keySet().iterator().next();
      List<List<String>> loadedFile = this.csvFile.get(firstKey);

      if (header != null && target != null && !this.csvFile.isEmpty()) {
        List<String> firstRow = loadedFile.get(0);
        for (int i = 0; i < firstRow.size(); i++) {
          if (firstRow.get(i).equals(header)) {
            this.searchByColumnIndex(loadedFile, target, i, responseMap);
          }
        }
      } else if (columnIndex != null && target != null && !this.csvFile.isEmpty()) {
        this.searchByColumnIndex(loadedFile, target, columnIndex, responseMap);
      } else if (target != null && !this.csvFile.isEmpty()) {
        for (int i = 0; i < loadedFile.size(); i++) {
          for (String word : loadedFile.get(i)) {
            if (word.equals(target)) {
              responseMap.put(String.valueOf(i), loadedFile.get(i));
              break;
            }
          }
        }
      }

      if (!responseMap.isEmpty()) {
        return new FileSuccessResponse(responseMap).serialize();
      } else {
        System.out.println("Response map empty!");
        return new FileFailureResponse().serialize();
      }
    } catch (Exception e) {
      System.out.println("error" + e);
      return new FileFailureResponse().serialize();
    }
  }

  /**
   * Searches file for target string by column index
   *
   * @param file to search
   * @param toSearch target
   * @param columnIndex
   * @param responseMap populated with found rows
   */
  public static void searchByColumnIndex(
      List<List<String>> file,
      String toSearch,
      Integer columnIndex,
      Map<String, Object> responseMap) {
    try {
      for (int i = 0; i < file.size(); i++) {
        if (file.get(i).get(columnIndex).equals(toSearch)) {
          responseMap.put(String.valueOf(i), file.get(i));
        }
      }
    } catch (IndexOutOfBoundsException e) {
      throw new IndexOutOfBoundsException("Error: searching using an invalid index!");
    }
  }

  /** Response object to send if file is searched successfully */
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
      } catch (JsonDataException e) {
        return new BadJsonFailureResponse().serialize();
      } catch (Exception e) {
        e.printStackTrace();
        throw e;
      }
    }
  }

  /** Response object to send if file failed to be searched */
  public record FileFailureResponse(String response_type) {
    public FileFailureResponse() {
      this("No file found");
    }

    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(FileFailureResponse.class).toJson(this);
    }
  }
}
