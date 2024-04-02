package com.github.artlibs.autotrace4j;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.artlibs.autotrace4j.context.AutoTraceCtx;
import com.github.artlibs.testcase.Const;
import com.github.artlibs.testcase.TpeCase;
import com.github.artlibs.testcase.Tuple;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RunTests {
    private final String expectedTraceId = "expected-trace-id";
    private final String expectedSpanId = "expected-span-id";
    private final String expectedParentSpanId = "expected-p-span-id";
    private final String httpBinOrgUrl = "https://httpbin.org/get";

    @BeforeAll
    public static void beforeAll() throws Exception {
        System.out.println("====== beforeAll ======");
        AutoTrace4j.premain("com.github.artlibs.testcase"
                , ByteBuddyAgent.install());
    }

    @Test
    void testSlf4jMDC() {
        // 01.Prepare
        AutoTraceCtx.setTraceId(expectedTraceId);
        AutoTraceCtx.setSpanId(expectedSpanId);
        AutoTraceCtx.setParentSpanId(expectedParentSpanId);

        // 02.When
        String realTraceId = MDC.get(Const.ATO_TRACE_ID);
        String realSpanId = MDC.get(Const.ATO_SPAN_ID);
        String realParentSpanId = MDC.get(Const.ATO_PARENT_SPAN_ID);

        // 03.Verify
        Assertions.assertEquals(expectedTraceId, realTraceId);
        Assertions.assertEquals(expectedSpanId, realSpanId);
        Assertions.assertEquals(expectedParentSpanId, realParentSpanId);
    }

    @Test
    void testThreadPoolExecutor() throws Exception {
        // 01.Prepare
        ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(5);
        ExecutorService scheduledThreadPoolExecutor = Executors.newScheduledThreadPool(5);

        // 02.When
        boolean tpe = TpeCase.newCase(threadPoolExecutor)
                .run(expectedTraceId, expectedSpanId);
        boolean stp = TpeCase.newCase(scheduledThreadPoolExecutor)
                .run(expectedTraceId, expectedSpanId);

        // 03.Verify
        Assertions.assertTrue(tpe);
        Assertions.assertTrue(stp);
    }

    @Test
    void testSunHttpClient() throws Exception {
        // 01.Prepare
        AutoTraceCtx.setTraceId(expectedTraceId);
        AutoTraceCtx.setSpanId(expectedSpanId);
        URL url = new URL(httpBinOrgUrl);

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
        Tuple tuple = extractTraceIdFromHeader(response.toString());

        // 03.Verify
        Assertions.assertEquals(200, responseCode);
        Assertions.assertEquals(expectedTraceId, tuple.getValue1());
        Assertions.assertEquals(expectedSpanId, tuple.getValue2());
    }

    @Test
    void testOkHttpClient() throws Exception {
        // 01.Prepare
        AutoTraceCtx.setTraceId(expectedTraceId);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(httpBinOrgUrl).build();

        // 02.When
        Response response = client.newCall(request).execute();
        boolean success = response.isSuccessful();
        ResponseBody body = response.body();
        String resp = Objects.requireNonNull(body).string();
        System.out.println("OkHttp out: " + resp);
        Tuple tuple = extractTraceIdFromHeader(resp);

        // 03.Verify
        Assertions.assertTrue(success);
        Assertions.assertEquals(expectedTraceId, tuple.getValue1());
        Assertions.assertEquals(expectedSpanId, tuple.getValue2());
    }

    @Test
    void testApacheHttpClient() throws Exception {
        // 01.Prepare
        AutoTraceCtx.setSpanId(expectedSpanId);
        AutoTraceCtx.setTraceId(expectedTraceId);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(httpBinOrgUrl);

        // 02.When
        CloseableHttpResponse respObj = httpClient.execute(httpGet);
        String response = EntityUtils.toString(respObj.getEntity());
        System.out.println("ApacheHttp out: " + response);
        Tuple tuple = extractTraceIdFromHeader(response);

        // 03.Verify
        Assertions.assertEquals(expectedTraceId, tuple.getValue1());
        Assertions.assertEquals(expectedSpanId, tuple.getValue2());
        Assertions.assertEquals(200, respObj.getStatusLine().getStatusCode());
    }

    private Tuple extractTraceIdFromHeader(String response) {
        JSONObject headerObj = JSON.parseObject(response)
                .getJSONObject("headers");
        Map<String, String> headerMap = new HashMap<>();
        for (String key : headerObj.keySet()) {
            headerMap.put(key.toLowerCase(), headerObj.getString(key));
        }
        return new Tuple(
                headerMap.get(Const.ATO_TRACE_ID.toLowerCase()),
                headerMap.get(Const.ATO_SPAN_ID.toLowerCase())
        );
    }
}
