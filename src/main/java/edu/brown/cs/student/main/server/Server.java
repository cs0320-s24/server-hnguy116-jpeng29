package edu.brown.cs.student.main.server;

import static spark.Spark.after;

import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.server.acsAPI.AcsHandler;
import edu.brown.cs.student.main.server.csvrequests.LoadCsvHandler;
import edu.brown.cs.student.main.server.csvrequests.SearchCsvHandler;
import edu.brown.cs.student.main.server.csvrequests.ViewCsvHandler;
import java.util.*;
import spark.Spark;

/** Main class for project Server. */
public class Server {

  // create state object to be shared through csv handlers
  private final Map<String, List<List<String>>> loadedCsv;
  private final int port = 5556;

  /**
   * Constructor for Server.
   *
   * @param loadedCsv state object
   */
  public Server(Map<String, List<List<String>>> loadedCsv) {
    this.loadedCsv = loadedCsv;

    Spark.port(this.port);

    after(
        (request, response) -> {
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "*");
        });

    Spark.get("loadcsv", new LoadCsvHandler(this.loadedCsv));
    try {
      Spark.get("viewcsv", new ViewCsvHandler(this.loadedCsv));
      Spark.get("searchcsv", new SearchCsvHandler(this.loadedCsv));
    } catch (NullPointerException e) {
      new FileNotLoadedFailureResponse().serialize();
    }
    Spark.get("broadband", new AcsHandler());

    Spark.init();
    Spark.awaitInitialization();
  }

  /** Main method: creates a new instance of Server and prints URL */
  public static void main(String[] args) {
    Server server = new Server(new HashMap<>());
    System.out.println("Server started at http://localhost:" + server.port);
  }

  /** Response object to send if the file is not loaded */
  public record FileNotLoadedFailureResponse(String response_type) {
    public FileNotLoadedFailureResponse() {
      this("File not loaded!");
    }
    /**
     * @return this response, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(Server.FileNotLoadedFailureResponse.class).toJson(this);
    }
  }
}