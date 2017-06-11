package cz.josefadamcik.trackontrakt;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.github.tomakehurst.wiremock.WireMockServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static cz.josefadamcik.trackontrakt.testutil.AssetReaderUtilKt.asset;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class WireMockApplicationTestCase {


    private Context applicationContext;

    /**
     * The @Rule - WireMockRule does NOT currently work for the ApplicationTestCase because it is not based on JUnit3 and not JUnit4 so we need to create & manage the WireMockServer ourselves
     * <p/>
     * As of 09.09.2015 - "To test an Android application object on the Android runtime you use the ApplicationTestCase class.
     * It is expected that Google will soon provide a special JUnit4 rule for testing the application object but at the moment his is not yet available."
     * <p/>
     * Reference: http://www.vogella.com/tutorials/AndroidTesting/article.html
     */
    WireMockServer wireMockServer = new WireMockServer(BuildConfig.MOCKSERVER_PORT);

    @Before
    public void setUp() {
        applicationContext = InstrumentationRegistry.getTargetContext().getApplicationContext();
        wireMockServer.start();
    }

    @After
    public void tearDown() throws Exception {
        wireMockServer.stop();
    }

    /**
     * Test WireMock, but just the Http Call.  Make sure the response matches the mock we want.
     */
    @Test
    public void testWiremockPlusOkHttp() throws IOException {
        Timber.d("testWiremockPlusOkHttp");

        String uri = "/history.json";

        String jsonBody = asset(applicationContext, "history.json");
        assertFalse(jsonBody.isEmpty());
        wireMockServer.stubFor(get(urlMatching(uri))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(jsonBody)));

        String serviceEndpoint = "http://127.0.0.1:" + BuildConfig.MOCKSERVER_PORT;
        Timber.d("WireMock Endpoint: " + serviceEndpoint);

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(serviceEndpoint + uri)
                .build();

        Response response = okHttpClient.newCall(request).execute();

        assertEquals(jsonBody, response.body().string());
    }

}
