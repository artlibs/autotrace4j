package com.github.artlibs.autotrace4j;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.artlibs.autotrace4j.ctx.AutoTraceCtx;
import net.bytebuddy.agent.ByteBuddyAgent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeAll;
import testing.artlibs.autotrace4j.jdk.ThreadPoolExecutorCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AutoTrace4jTest {
    private final String expectedTraceId = "expected-trace-id";
    private final String expectedSpanId = "expected-span-id";
    private final String expectedParentSpanId = "expected-p-span-id";

    @BeforeAll
    public static void beforeAll() throws Exception {
        AutoTrace4j.premain("testing.artlibs.autotrace4j"
                , ByteBuddyAgent.install());
    }

    @Test
    public void testThreadPoolExecutor() throws Exception {
        // 01.Prepare
        ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(5);
        ExecutorService scheduledThreadPoolExecutor = Executors.newScheduledThreadPool(5);

        // 02.When
        boolean tpe = ThreadPoolExecutorCase.newCase(
                threadPoolExecutor
        ).run(expectedTraceId);
        boolean stp = ThreadPoolExecutorCase.newCase(
                scheduledThreadPoolExecutor
        ).run(expectedTraceId);

        // 02.Verify
        Assertions.assertTrue(tpe);
        Assertions.assertTrue(stp);
    }

    @Test
    public void testSunHttpClient() throws Exception {
        // 01.Prepare
        AutoTraceCtx.setTraceId(expectedTraceId);
        URL url = new URL("http://httpbin.org/get");

        // 02.When
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        System.out.println("SunHttp out: " + response);
        String actualTraceId = extractTraceIdFromHeader(response.toString());

        // 03.Verify
        Assertions.assertEquals(200, responseCode);
        Assertions.assertEquals(expectedTraceId, actualTraceId);
    }

    @Test
    public void testOkHttpClient() throws Exception {
        // 01.Prepare
        AutoTraceCtx.setTraceId(expectedTraceId);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://httpbin.org/get")
                .build();

        // 02.When
        Response response = client.newCall(request).execute();
        boolean success = response.isSuccessful();
        ResponseBody body = response.body();
        String resp = Objects.requireNonNull(body).string();
        System.out.println("OkHttp out: " + resp);
        String actualTraceId = extractTraceIdFromHeader(resp);

        // 03.Verify
        Assertions.assertTrue(success);
        Assertions.assertEquals(expectedTraceId, actualTraceId);
    }

    @Test
    public void testApacheHttpClient() throws Exception {
        // 01.Prepare
        AutoTraceCtx.setTraceId(expectedTraceId);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://httpbin.org/get");

        // 02.When
        CloseableHttpResponse respObj = httpClient.execute(httpGet);
        String response = EntityUtils.toString(respObj.getEntity());
        System.out.println("ApacheHttp out: " + response);
        String actualTraceId = extractTraceIdFromHeader(response);

        // 03.Verify
        Assertions.assertEquals(expectedTraceId, actualTraceId);
        Assertions.assertEquals(200, respObj.getStatusLine().getStatusCode());
    }

    private String extractTraceIdFromHeader(String response) {
        JSONObject headerObj = JSON.parseObject(response)
                .getJSONObject("headers");
        Map<String, String> headerMap = new HashMap<>();
        for (String key : headerObj.keySet()) {
            headerMap.put(key.toLowerCase(), headerObj.getString(key));
        }
        return headerMap.get(AutoTraceCtx.ATO_TRACE_ID.toLowerCase());
    }
}
