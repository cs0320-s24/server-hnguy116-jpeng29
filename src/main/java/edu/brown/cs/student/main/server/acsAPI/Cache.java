package edu.brown.cs.student.main.server.acsAPI;

import com.google.common.cache.*;
import com.squareup.moshi.*;
import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import okio.Buffer;

public class Cache {
  private LoadingCache<URL, List<List<String>>> cache;

  public Cache(Specification specification, int amount) {
    if (specification == Specification.TIME_ACCESS) {
      this.timeAccessCache(amount);
    } else if (specification == Specification.TIME_WRITE) {
      this.timeWriteCache(amount);
    } else {
      this.sizeCache(amount);
    }
  }

  public void timeAccessCache(int amount) {
    this.cache =
        CacheBuilder.newBuilder()
            .expireAfterAccess(amount, TimeUnit.MINUTES)
            .build(
                new CacheLoader<>() {
                  @Override
                  public List<List<String>> load(URL url) throws DatasourceException, IOException {
                    return retrieveJson(url);
                  }
                });
  }

  public void timeWriteCache(int amount) {
    this.cache =
        CacheBuilder.newBuilder()
            .expireAfterWrite(amount, TimeUnit.MINUTES)
            .build(
                new CacheLoader<>() {
                  @Override
                  public List<List<String>> load(URL url) throws DatasourceException, IOException {
                    return retrieveJson(url);
                  }
                });
  }

  public void sizeCache(int amount) {
    this.cache =
        CacheBuilder.newBuilder()
            .maximumSize(amount)
            .build(
                new CacheLoader<>() {
                  @Override
                  public List<List<String>> load(URL url) throws DatasourceException, IOException {
                    return retrieveJson(url);
                  }
                });
  }

  public List<List<String>> get(URL url) {
    return cache.getUnchecked(url);
  }

  public ConcurrentMap<URL, List<List<String>>> getCache() {
    return this.cache.asMap();
  }

  public static List<List<String>> retrieveJson(URL requestURL)
      throws DatasourceException, IOException {
    HttpURLConnection clientConnection = connect(requestURL);
    Moshi moshi = new Moshi.Builder().build();

    JsonAdapter<List<List<String>>> adapter =
        moshi.adapter(
            Types.newParameterizedType(
                List.class, Types.newParameterizedType(List.class, String.class)));

    List<List<String>> body =
        adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    clientConnection.disconnect();
    if (body == null) {
      throw new DatasourceException("Malformed response from ACS");
    }
    return body;
  }

  private static HttpURLConnection connect(URL requestURL) throws DatasourceException, IOException {
    URLConnection urlConnection = requestURL.openConnection();
    if (!(urlConnection instanceof HttpURLConnection clientConnection)) {
      throw new DatasourceException("unexpected: result of connection wasn't HTTP");
    }
    clientConnection.connect(); // GET
    if (clientConnection.getResponseCode() != 200) {
      throw new DatasourceException(
          "unexpected: API connection not success status " + clientConnection.getResponseMessage());
    }
    return clientConnection;
  }
}
