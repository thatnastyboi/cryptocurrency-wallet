package bg.sofia.uni.fmi.mjt.wallet.crypto.response;

import java.net.URI;
import java.net.URISyntaxException;

public class Query {
    private final String apiKey;
    private static final String AUTHORITY = "rest.coinapi.io";
    private static final String PATH = "/v1/assets/";
    private static final String API_CALL_IDENTIFIER = "APIKEY-";

    public Query(String apiKey) {
        this.apiKey = apiKey;
    }

    public URI constructUri() throws URISyntaxException {
        String pathWithKey = PATH + API_CALL_IDENTIFIER + apiKey;

        return new URI("https", AUTHORITY,
            pathWithKey, null);
    }
}
