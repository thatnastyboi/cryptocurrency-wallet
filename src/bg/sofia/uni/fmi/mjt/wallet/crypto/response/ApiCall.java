package bg.sofia.uni.fmi.mjt.wallet.crypto.response;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ApiCall {

    private final HttpClient httpClient;
    private final String apiKey;
    private static final int MINUTES_OF_RESPONSE_VALIDITY = 30;
    private static final int MAX_RESULTS = 50;
    private static final double MINIMUM_PRICE_FOR_ONE = 0.0001;
    private static final int MAXIMUM_PRICE_FOR_ONE = 100_000;
    private Map<String, Double> marketChart;
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public ApiCall(HttpClient httpClient, String apiKey) {
        this.httpClient = httpClient;
        this.apiKey = apiKey;
        this.marketChart = new HashMap<>();
    }

    public Map<String, Double> getMarketChart() {
        if (marketChart.isEmpty()) {
            makeApiCall(new Query(apiKey));
        }

        return marketChart;
    }

    private void makeApiCall(Query query) {
        try {
            URI uriAll = query.constructUri();

            HttpRequest request = HttpRequest.newBuilder().uri(uriAll).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();

            int ctr = 0;
            while (ctr < MAX_RESULTS) {
                JsonObject current = jsonArray.get(++ctr).getAsJsonObject();
                if (current.get("type_is_crypto").getAsInt() != 1 || !current.has("price_usd")
                    || Double.compare(current.get("price_usd").getAsDouble(), MINIMUM_PRICE_FOR_ONE) < 0
                    || Double.compare(current.get("price_usd").getAsDouble(), MAXIMUM_PRICE_FOR_ONE) > 0) {
                    jsonArray.remove(ctr--);
                    continue;
                }

                marketChart.put(current.get("asset_id").getAsString(),
                    Double.valueOf(current.get("price_usd").getAsDouble()));
            }

            scheduler.schedule(() -> scheduleApiDeletion(), MINUTES_OF_RESPONSE_VALIDITY, TimeUnit.MINUTES);

        } catch (Exception e) {
            throw new RuntimeException("An error has occurred when making a query to API");
        }
    }

    private void scheduleApiDeletion() {
        marketChart.clear();
    }

    public void shutdownScheduler() {
        scheduler.shutdownNow();
    }
}
