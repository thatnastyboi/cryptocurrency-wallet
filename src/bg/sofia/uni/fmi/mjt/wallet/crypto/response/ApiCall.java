package bg.sofia.uni.fmi.mjt.wallet.crypto.response;

import bg.sofia.uni.fmi.mjt.wallet.crypto.exception.FailedRequestException;
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
    private static final int BAD_REQUEST_CODE = 400;
    private static final int INTERNET_SERVER_ERROR_CODE = 500;
    private Map<String, Double> marketChart;
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public ApiCall(HttpClient httpClient, String apiKey) {
        this.httpClient = httpClient;
        this.apiKey = apiKey;
        this.marketChart = new HashMap<>();
    }

    public Map<String, Double> getMarketChart() throws FailedRequestException {
        if (marketChart.isEmpty()) {
            makeApiCall(new Query(apiKey));
        }

        return marketChart;
    }

    private void makeApiCall(Query query) throws FailedRequestException {
        try {
            URI uriAll = query.constructUri();

            HttpRequest request = HttpRequest.newBuilder().uri(uriAll).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int responseCode = response.statusCode();
            System.out.println(uriAll);
            System.out.println(responseCode);

            handleResponseCode(responseCode);

            JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();

            fetchMarketChart(jsonArray);

            scheduler.schedule(() -> scheduleApiDeletion(), MINUTES_OF_RESPONSE_VALIDITY, TimeUnit.MINUTES);

        } catch (FailedRequestException e) {
            throw new FailedRequestException(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getClass());
            throw new RuntimeException("An error has occurred when making a query to API");
        }
    }

    private void fetchMarketChart(JsonArray jsonArray) {
        int ctr = 0;
        System.out.println(jsonArray.size());
        while (ctr < jsonArray.size() && ctr < MAX_RESULTS) {
            JsonObject current = jsonArray.get(ctr++).getAsJsonObject();

            if (current.get("type_is_crypto").getAsInt() != 1 || !current.has("price_usd")
                || Double.compare(current.get("price_usd").getAsDouble(), MINIMUM_PRICE_FOR_ONE) < 0
                || Double.compare(current.get("price_usd").getAsDouble(), MAXIMUM_PRICE_FOR_ONE) > 0) {
                jsonArray.remove(ctr);
                continue;
            }
            marketChart.put(current.get("asset_id").getAsString(),
                Double.valueOf(current.get("price_usd").getAsDouble()));
        }
    }

    private void handleResponseCode(int responseCode) throws FailedRequestException {
        if (responseCode >= BAD_REQUEST_CODE && responseCode < INTERNET_SERVER_ERROR_CODE) {
            throw new FailedRequestException("Could not fetch error because of a client side error");
        } else if (responseCode >= INTERNET_SERVER_ERROR_CODE) {
            throw new FailedRequestException("Could not fetch error because of a server side error");
        }
    }

    private void scheduleApiDeletion() {
        marketChart.clear();
    }

    public void shutdownScheduler() {
        scheduler.shutdownNow();
    }
}
