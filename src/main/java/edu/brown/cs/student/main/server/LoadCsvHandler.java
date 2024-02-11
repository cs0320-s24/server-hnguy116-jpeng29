package edu.brown.cs.student.main.server;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.csv.CsvParser;
import edu.brown.cs.student.main.csv.creatorfromrow.CreatorFromRow;
import edu.brown.cs.student.main.csv.creatorfromrow.ParsedObject;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class LoadCsvHandler implements Route {

  private List<List<String>> loadedCsv;
  private static CsvParser<List<String>> MY_PARSER;
  private static CreatorFromRow<List<String>> MY_PARSED_OBJECT;

  public LoadCsvHandler() {
    this.MY_PARSED_OBJECT = new ParsedObject();
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    try {
      String filename = request.queryParams("csvFile");

      FileReader fileReader = new FileReader(filename);
      this.MY_PARSER = new CsvParser(fileReader, this.MY_PARSED_OBJECT);
      this.loadedCsv = this.MY_PARSER.parse();

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
