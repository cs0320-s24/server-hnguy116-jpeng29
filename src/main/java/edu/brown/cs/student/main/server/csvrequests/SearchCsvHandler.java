package edu.brown.cs.student.main.server.csvrequests;

import com.squareup.moshi.*;
import edu.brown.cs.student.main.csv.creatorfromrow.CreatorFromRow;
import edu.brown.cs.student.main.server.csvrequests.LoadCsvHandler.BadJsonFailureResponse;
import java.util.*;
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
      String target = request.queryParams("target");
      String columnIndexString = request.queryParams("columnIndex");
      Integer columnIndex = null;
      if (columnIndexString != null) {
        columnIndex = Integer.parseInt(columnIndexString);
      }

      //      try {
      //         =
      //      } catch (BadMessageException e) {
      //        target = request.queryParams("target");
      //      }

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

  /** Response object to send if someone requested soup from an empty Menu */
  public record SoupNoRecipesFailureResponse(String response_type) {
    public SoupNoRecipesFailureResponse() {
      this("No file found");
    }

    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(SoupNoRecipesFailureResponse.class).toJson(this);
    }
  }
}
