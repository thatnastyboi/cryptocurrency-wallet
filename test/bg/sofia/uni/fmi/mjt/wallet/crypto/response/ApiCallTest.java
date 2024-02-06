package bg.sofia.uni.fmi.mjt.wallet.crypto.response;

import bg.sofia.uni.fmi.mjt.wallet.crypto.exception.FailedRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApiCallTest {
    private static final String DUMMY_API_KEY = "dummy-api-key";

    @Mock
    private HttpClient mockClient;
    @InjectMocks
    private ApiCall mockApiCall;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetMarketChart()
        throws FailedRequestException, IOException, InterruptedException {

        String testJsonArray = """
            [
               {
                  "asset_id": "DUMMY",
                  "type_is_crypto": 1,
                  "price_usd": 0.001
               },
               {
                  "asset_id": "MUMMY",
                  "type_is_crypto": 1,
                  "price_usd": 0.002
               },
               {
                  "asset_id": "TUMMY",
                  "type_is_crypto": 1,
                  "price_usd": 1242.222
               }
            ]
            """;

        System.out.println(testJsonArray);

        HttpResponse mockResponse = mock();
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);
        when(mockResponse.body()).thenReturn(testJsonArray);

        when(mockResponse.statusCode()).thenReturn(200);

        int actualSize = mockApiCall.getMarketChart().size();

        assertEquals(3, actualSize,
            "Expected 3 assets and their respective prices but were" + actualSize);
    }

    @Test
    void testGetMarketChartThrowsExceptionWhenCode4xx() throws IOException, InterruptedException {

        Map<String, Double> dummyMap = new HashMap<>();
        dummyMap.put("DUMMY", 0.001);
        dummyMap.put("MUMMY", 0.002);
        dummyMap.put("TUMMY", 1242.222);

        HttpResponse mockResponse = mock();
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(400);

        assertThrows(FailedRequestException.class,
            () -> mockApiCall.getMarketChart(),
            "Expected FailedRequestException but was not thrown");
    }

    @Test
    void testGetMarketChartThrowsExceptionWhenCode5xx() throws IOException, InterruptedException {

        HttpResponse mockResponse = mock();
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(500);

        assertThrows(FailedRequestException.class,
            () -> mockApiCall.getMarketChart(),
            "Expected FailedRequestException but was not thrown");
    }
}
