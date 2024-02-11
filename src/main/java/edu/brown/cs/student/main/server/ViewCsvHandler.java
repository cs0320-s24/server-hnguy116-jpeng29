package edu.brown.cs.student.main.server;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.csv.CsvParser;
import edu.brown.cs.student.main.csv.creatorfromrow.CreatorFromRow;
import edu.brown.cs.student.main.csv.creatorfromrow.ParsedObject;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class ViewCsvHandler implements Route {

  private Map<String, List<List<String>>> csvFile;

  public ViewCsvHandler() {

  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    try {
      //String filename = request.queryParams("csvFile");

      this.csvFile = getLoadedCsv();

      Map<String, Object> responseMap = new HashMap<>();
      responseMap.put(filename, this.loadedCsv);

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

  public List<List<String>> getLoadedCsv() {
    if (this.loadedCsv == null) {
      this.unmodifiableParsedObject = Collections.unmodifiableList(this.loadedCsv);
    }
    return this.unmodifiableParsedObject;
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

