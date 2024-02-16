package edu.brown.cs.student.main.server.csvrequests;

import com.squareup.moshi.*;
import edu.brown.cs.student.main.csv.CsvParser;
import edu.brown.cs.student.main.csv.creatorfromrow.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import spark.*;

/** Loads Csv as requested by */
public class LoadCsvHandler implements Route {

  private Map<String, List<List<String>>> loadedCsv;
  private Map<String, List<List<String>>> internalLoadedCsv;
  private static CsvParser<List<String>> MY_PARSER;
  private static CreatorFromRow<List<String>> MY_PARSED_OBJECT;

  /**
   * Constructor for LoadCsvHandler
   *
   * @param csvFile Map passed in through Server to contain parsed file
   */
  public LoadCsvHandler(Map<String, List<List<String>>> csvFile) {
    this.MY_PARSED_OBJECT = new ParsedObject();
    this.loadedCsv = csvFile;
    this.internalLoadedCsv = new HashMap<>();
  }

  /**
   * Reads and parses a file, returning a success or failure message to the user depending on the
   * method's success
   *
   * @param request
   * @param response
   * @return serialized response to user
   */
  @Override
  public Object handle(Request request, Response response) {
    try {
      String filename = request.queryParams("filepath");
      this.tryOpenFile(filename);
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
        System.out.println("responsemap empty");
        return new FileNotFoundFailureResponse().serialize();
      }
    } catch (Exception e) {
      System.out.println("exception e");
      return new FileNotFoundFailureResponse().serialize();
    }
  }

  /** Creates a new unmodifiable map, copying the contents of the internalLoadedCsv */
  public void loadCsv() {
    if (!this.loadedCsv.isEmpty()) {
      this.internalLoadedCsv = Collections.unmodifiableMap(this.loadedCsv);
      this.loadedCsv = new HashMap<>(this.internalLoadedCsv);
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
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<FileSuccessResponse> adapter = moshi.adapter(FileSuccessResponse.class);
        return adapter.toJson(this);
      } catch (Exception e) {
        e.printStackTrace();
        throw e;
      }
    }
  }

  /** Response object to send if the file is not found */
  public record FileNotFoundFailureResponse(String response_type) {
    public FileNotFoundFailureResponse() {
      this("error_datasource");
    }
    /**
     * @return this response, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(FileNotFoundFailureResponse.class).toJson(this);
    }
  }

  /** Response object to send if the file is outside the data directory */
  public record FileSecurityFailureResponse(String response_type) {
    public FileSecurityFailureResponse() {
      this("error_datasource");
    }
    /**
     * @return this response, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(FileSecurityFailureResponse.class).toJson(this);
    }
  }

  /**
   * throws SecurityException if attempting to access a file that's not in the data directory
   *
   * @param filename name of the file to parse
   * @throws SecurityException when file doesn't belong to data directory
   * @throws IOException for failed or interrupted I/O operations
   */
  private void tryOpenFile(String filename) throws IOException {
    File file = new File(filename);
    boolean fileInDataDirectory =
        file.getCanonicalPath().startsWith(System.getProperty("user.dir") + "/data/");
    if (!fileInDataDirectory) {
      System.out.println("file outside of directory");
      throw new SecurityException("Error: attempting to access restricted file!");
    }
  }
}
