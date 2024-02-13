package edu.brown.cs.student.main.server;

import com.squareup.moshi.*;
import edu.brown.cs.student.main.csv.CsvParser;
import edu.brown.cs.student.main.csv.creatorfromrow.*;
import java.io.FileReader;
import java.util.*;
import spark.*;

public class LoadCsvHandler implements Route {

  private Map<String, List<List<String>>> loadedCsv;
  private static CsvParser<List<String>> MY_PARSER;
  private static CreatorFromRow<List<String>> MY_PARSED_OBJECT;
  // private Map<String, List<List<String>>> unmodifiableParsedObject;

  public LoadCsvHandler(Map<String, List<List<String>>> csvFile) {
    this.MY_PARSED_OBJECT = new ParsedObject();
    this.loadedCsv = csvFile;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    try {
      String filename = request.queryParams("csvFile");

      FileReader fileReader = new FileReader(filename);
      this.MY_PARSER = new CsvParser(fileReader, this.MY_PARSED_OBJECT);
      List<List<String>> loadedFile = this.MY_PARSER.parse();

      Map<String, Object> responseMap = new HashMap<>();
      responseMap.put(filename, loadedFile);
      this.loadedCsv.put(filename, loadedFile);

      if (!responseMap.isEmpty()) {
        this.loadCsv();
        return new FileSuccessResponse().serialize();
      } else {
        System.out.println("oh no");
        return new FileNotFoundFailureResponse().serialize();
      }
    } catch (Exception e) {
      System.out.println("error" + e);
      return new FileNotFoundFailureResponse().serialize();
    }
  }

  public void loadCsv() {
    if (!this.loadedCsv.isEmpty()) {
      this.loadedCsv = Collections.unmodifiableMap(this.loadedCsv);
    }
  }

  /** Response object to send when the file is loaded successfully */
  public record FileSuccessResponse(String response_type) {
    public FileSuccessResponse() {
      this("Your file was loaded successfully!");
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

  /** Response object to send if the file is not found */
  public record FileNotFoundFailureResponse(String response_type) {
    public FileNotFoundFailureResponse() {
      this("error");
    }

    /**
     * @return this response, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(FileNotFoundFailureResponse.class).toJson(this);
    }
  }
}
