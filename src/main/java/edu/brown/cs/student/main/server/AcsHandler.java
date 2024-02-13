package edu.brown.cs.student.main.server;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import spark.Request;
import spark.Response;
import spark.Route;

// retrieve the percentage of households with broadband access for a target location
// //by providing the name of the target state and county in my request

// the response should include the date and time that all data was retrieved from the ACS API by
// your API server, as well as the state and county names your server received.

public class AcsHandler implements Route {
  @Override
  public Object handle(Request request, Response response) {
    String county = request.queryParams("county");
    String state = request.queryParams("state");

    Map<String, Object> responseMap = new HashMap<>();
    try {
      // Sends a request to the API and receives JSON back
      String activityJson = this.sendRequest();
      // Deserializes JSON into an Activity
      // Activity activity = ActivityAPIUtilities.deserializeActivity(activityJson);
      // Adds results to the responseMap
      responseMap.put("result", "success");
      // responseMap.put("acs", activity);
      return responseMap;
    } catch (Exception e) {
      e.printStackTrace();
      // This is a relatively unhelpful exception message. An important part of this sprint will be
      // in learning to debug correctly by creating your own informative error messages where Spark
      // falls short.
      responseMap.put("result", "Exception");
    }
    return responseMap;
  }

  private String sendRequest() throws URISyntaxException, IOException, InterruptedException {
    HttpRequest buildBoredApiRequest =
        HttpRequest.newBuilder()
            .uri(new URI("http://www.boredapi.com/api/activity/"))
            .GET()
            .build();

    // Send that API request then store the response in this variable. Note the generic type.
    HttpResponse<String> sentBoredApiResponse =
        HttpClient.newBuilder()
            .build()
            .send(buildBoredApiRequest, HttpResponse.BodyHandlers.ofString());

    // What's the difference between these two lines? Why do we return the body? What is useful from
    // the raw response (hint: how can we use the status of response)?
    System.out.println(sentBoredApiResponse);
    System.out.println(sentBoredApiResponse.body());

    return sentBoredApiResponse.body();
  }
}
