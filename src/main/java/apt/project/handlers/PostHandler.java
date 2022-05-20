package apt.project.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import apt.project.util.GenericException;
import apt.project.util.Hash;
import org.json.JSONArray;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

public class PostHandler implements HttpHandler {

    Jedis redisDb;
    private static final int linksPerPage = 10;

    public PostHandler(Jedis redisDb) {
        this.redisDb = redisDb;
    }

    public void handle(HttpExchange t) {

        InputStream is;
        OutputStream os = null;
        try {

            is = t.getRequestBody();
            os = t.getResponseBody();

            if (t.getRequestHeaders().getFirst("Content-Type") == null) {
                throw new GenericException(400, "Content-Type header is missing.\n");
            } else if (!t.getRequestHeaders().getFirst("Content-Type").equals("application/json")) {
                throw new GenericException(400, "Content-Type header must be set to application/json\n");
            }

            InputStreamReader inputStreamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
            StringBuilder sb = new StringBuilder();
            String response;

            // Reading the stream.
            int i;
            try {
                while ((i = inputStreamReader.read()) >= 0) {
                    sb.append((char) i);
                }
            } catch (IOException e) {
                throw new GenericException(500, "Internal server error");
            }

            // Trying to parse the request as a json object.
            if (!sb.isEmpty()) {

                System.out.printf("handling a new request. request: %s, address: %s\n", sb, t.getRemoteAddress().getAddress());

                JSONObject jsonRequest = new JSONObject(sb.toString());
                Map mapRequest = jsonRequest.toMap();

                if (!mapRequest.containsKey("search") || !mapRequest.containsKey("page")) {
                    throw new GenericException(400, "JSON request missing search field.");
                }

                int page = Integer.parseInt(mapRequest.get("page").toString());
                String fingerPrint = Hash.hashUserAndSearchPhrase(t.getRemoteAddress().getAddress().toString(), mapRequest.get("search").toString());

                if (redisDb.hmget("sessions", fingerPrint).get(0) == null ) {

                    System.out.println(fingerPrint);
                    /*
                    * Khaled => put your query here instead of the dummy.
                    * */

                    String []sites = {
                            "www.google.com",
                            "www.facebook.com",
                            "www.test.com",
                            "www.twitter.com",
                            "www.reddit.com",
                            "www.youtube.com",
                            "www.cnn.com",
                            "www.yahoo.com",
                            "www.gmail.com",
                            "www.amazon.com",
                            "www.amazon.eg",
                            "aws.amazon.com",
                    };

                    JSONArray ja = new JSONArray();
                    JSONArray redisArray = new JSONArray();

                    for (i = (page - 1) * linksPerPage; i < page * linksPerPage && i < sites.length; i++) {
                        JSONObject jo = new JSONObject();

                        jo.put("link", sites[i]);
                        jo.put("title", "Testing title");
                        jo.put("description", "testing description");

                        ja.put(jo);
                    }

                    // If there's more than 10 links in this result, then insert into redis all of them, and send the first 10.
                    if (sites.length > linksPerPage) {
                        for (i = 0; i < sites.length; i++) {
                            JSONObject jo = new JSONObject();

                            jo.put("link", sites[i]);
                            jo.put("title", "Testing title");
                            jo.put("description", "testing description");

                            redisArray.put(jo);
                        }

                        redisDb.hmset("sessions", Map.ofEntries(
                                entry(fingerPrint, redisArray.toString())
                        ));
                        System.out.printf("Inserted into redis: %s\n", redisArray.toString());
                    }

                    JSONObject responseObject = new JSONObject();
                    responseObject.put("result", ja);
                    responseObject.put("number", sites.length);

                    response = responseObject.toString();
                    System.out.printf("response: %s\n\n", response);
                } else {

                    // Access redis.
                    System.out.printf("access to cached. fingerprint: %s, page: %d\n", fingerPrint, page);

                    List<String> results = redisDb.hmget("sessions", fingerPrint);

                    JSONArray fullResult = new JSONArray(results.get(0));
                    JSONArray jsonResponseArray = new JSONArray();
                    JSONObject responseObject = new JSONObject();

                    for (i = (page - 1) * linksPerPage; i < page * linksPerPage && i < fullResult.length(); i++) {
                        jsonResponseArray.put(fullResult.getJSONObject(i));
                    }

                    responseObject.put("result", jsonResponseArray);
                    responseObject.put("number", fullResult.length());

                    response = responseObject.toString();
                    System.out.printf("response: %s\n\n", response);
                }

                // Setting Headers, and sending the response.
                t.getResponseHeaders().put("Content-Type", Collections.singletonList("application/json"));
                try {
                    t.sendResponseHeaders(200, response.length());
                    os.write(response.getBytes());
                } catch (Exception e) {
                    throw new GenericException(500, "Internal server error");
                }
            } else {
                response = "empty query";
                t.sendResponseHeaders(400, response.length());
                os.write(response.getBytes());
            }
        } catch (GenericException e) {

            e.printStackTrace();
            try {
                t.sendResponseHeaders(400, e.getMessage().length());
                if (os != null)
                    os.write(e.getMessage().getBytes());
            } catch (Exception x) {
                x.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                t.close();
            } catch (Exception i) {
                i.printStackTrace();
            }
        }
    }
}
