package edu.brown.cs.student.main.server;

import static spark.Spark.after;

import com.squareup.moshi.Moshi;
import java.util.*;
import spark.Spark;

public class Server {

  private final Map<String, List<List<String>>> loadedCsv;
  private final int port = 5556;

  public Server(Map<String, List<List<String>>> loadedCsv) {
    this.loadedCsv = loadedCsv;

    Spark.port(port);

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
      //      new FileNotLoadedFailureResponse().serialize();
    }

    Spark.init();
    Spark.awaitInitialization();
  }

  public static void main(String[] args) {
    Server server = new Server(new HashMap<>());
    System.out.println("Server started at http://localhost:" + server.port);
  }

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
