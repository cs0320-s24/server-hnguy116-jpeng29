package edu.brown.cs.student.main.server;

import com.squareup.moshi.*;
import edu.brown.cs.student.main.csv.creatorfromrow.CreatorFromRow;
import java.util.*;
import org.eclipse.jetty.http.BadMessageException;
import spark.*;

public class SearchCsvHandler implements Route {

  private Map<String, List<List<String>>> csvFile;
  private CreatorFromRow<List<String>> row;

  public SearchCsvHandler(Map<String, List<List<String>>> loadedCsv) {
    this.csvFile = loadedCsv;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    try {
      String header = request.queryParams("header");
      String target = null;
      String columnIndexString = request.queryParams("columnIndex");
      Integer columnIndex = null;
      if (columnIndexString != null) {
        columnIndex = Integer.parseInt(columnIndexString);
      }

      try {
        target = request.queryParams("target");
      } catch (BadMessageException e) {
        target = request.queryParams("target");
      }

      Map<String, Object> responseMap = new HashMap<>();
      String firstKey = this.csvFile.keySet().iterator().next();
      List<List<String>> loadedFile = this.csvFile.get(firstKey);

      if (header != null && target != null && !this.csvFile.isEmpty()) {
        List<String> firstRow = loadedFile.get(0);
        for (int i = 0; i < firstRow.size(); i++) {
          if (firstRow.get(i).equals(header)) {
            this.searchByColumnIndex(loadedFile, target, i, responseMap);
            System.out.println(firstRow);
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
        System.out.println("???");
        return new SoupNoRecipesFailureResponse().serialize();
      }
    } catch (Exception e) {
      System.out.println("error" + e);
      return new SoupNoRecipesFailureResponse().serialize();
    }
  }

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
