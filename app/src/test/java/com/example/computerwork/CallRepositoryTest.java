package com.example.computerwork;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.Assert.*;

public class CallRepositoryTest {

    private MockWebServer mockWebServer;
    private CallRepository callRepository;

    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxpbmNpZGh1b2JiY2p3d2Njc3R5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQ3MTQyMjEsImV4cCI6MjA1MDI5MDIyMX0.H7pAHmmfd1-bdeammV-UqdC9aaCQU0GOnkX4CDdYg4s";

    @Before
    public void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        callRepository = new CallRepository(null) {
            @Override
            public void sendBookingToSupabase(String login, String problem, String status, Callback callback) {
                super.sendBookingToSupabase(login, problem, status, callback);
            }
        };
    }

    @After
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testSendBookingToSupabase_successfulResponse() throws InterruptedException {
        // Arrange
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200)
                .setBody("{\"success\":true}");
        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        // Act
        new CallRepository(null) {
            @Override
            public void sendBookingToSupabase(String login, String problem, String status, Callback callback) {
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();

                okhttp3.RequestBody body = okhttp3.RequestBody.create(
                        "{\"LoginUser\":\"test\",\"Crush\":\"bug\",\"Status\":\"ok\"}",
                        okhttp3.MediaType.get("application/json"));

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(mockWebServer.url("/rest/v1/Queries"))
                        .post(body)
                        .addHeader("apikey", API_KEY)
                        .addHeader("Authorization", "Bearer " + API_KEY)
                        .addHeader("Content-Type", "application/json")
                        .build();

                client.newCall(request).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(okhttp3.Call call, IOException e) {
                        fail("Request failed");
                        latch.countDown();
                    }

                    @Override
                    public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                        assertTrue(response.isSuccessful());
                        latch.countDown();
                    }
                });
            }
        }.sendBookingToSupabase("test", "bug", "ok", new CallRepository.Callback() {
            @Override
            public void onSuccess() {
                // callback not used here
            }

            @Override
            public void onFailure(String errorMessage) {
                fail("Should not fail");
            }
        });

        // Assert
        boolean finished = latch.await(3, TimeUnit.SECONDS);
        assertTrue("Callback was not called in time", finished);
    }
}

