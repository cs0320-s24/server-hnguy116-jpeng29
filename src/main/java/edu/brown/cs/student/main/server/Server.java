package edu.brown.cs.student.main.server;

import static spark.Spark.after;

import java.util.*;
import spark.Spark;

public class Server {

  private final Map<String, List<List<String>>> loadedCsv;
  private final int port = 5555;

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
    } catch (NullPointerException e) {
      // display error message to user
    }

    Spark.init();
    Spark.awaitInitialization();
  }

  public static void main(String[] args) {
    Server server = new Server(new HashMap<>());
    System.out.println("Server started at http://localhost:" + server.port);
  }
}
