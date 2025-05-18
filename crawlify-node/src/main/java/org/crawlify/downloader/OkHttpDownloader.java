package org.crawlify.downloader;

import okhttp3.CookieJar;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody; // OkHttp's RequestBody
import okhttp3.Response; // OkHttp's Response
import okhttp3.ResponseBody; // OkHttp's ResponseBody
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request; // WebMagic's Request
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.AbstractDownloader;
import us.codecraft.webmagic.proxy.Proxy; // WebMagic's Proxy
import us.codecraft.webmagic.proxy.ProxyProvider;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.utils.CharsetUtils;
import us.codecraft.webmagic.utils.HttpClientUtils; // May need similar for OkHttp headers
import us.codecraft.webmagic.utils.HttpConstant;


import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OkHttpDownloader extends AbstractDownloader implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(OkHttpDownloader.class);
    private OkHttpClient okHttpClient; // Single instance
    private ProxyProvider proxyProvider;
    private boolean responseHeader = true; // similar to HttpClientDownloader

    public OkHttpDownloader() {
        this(new OkHttpClient.Builder());
    }

    public OkHttpDownloader(OkHttpClient.Builder builder) {
        // Configure the OkHttpClient instance here
        // e.g., timeouts, cookieJar, proxySelector, authenticator

        this.okHttpClient = builder
                .connectTimeout(30, TimeUnit.SECONDS) // Example timeout
                .readTimeout(30, TimeUnit.SECONDS)    // Example timeout
                .writeTimeout(30, TimeUnit.SECONDS)   // Example timeout
                .cookieJar(CookieJar.NO_COOKIES)      // Replace with a proper CookieJar if needed
                .build();
    }

    public void setProxyProvider(ProxyProvider proxyProvider) {
        this.proxyProvider = proxyProvider;
    }

    @Override
    public Page download(Request request, Task task) {
        if (task == null || task.getSite() == null) {
            throw new NullPointerException("task or site can not be null");
        }
        Site site = task.getSite();
        OkHttpClient clientToUse = this.okHttpClient;
        Proxy webmagicProxy = proxyProvider != null ? proxyProvider.getProxy(request, task) : null;

        if (webmagicProxy != null) {
            OkHttpClient.Builder clientBuilder = this.okHttpClient.newBuilder();
            java.net.Proxy proxy = new java.net.Proxy(java.net.Proxy.Type.HTTP,
                    new java.net.InetSocketAddress(webmagicProxy.getHost(), webmagicProxy.getPort()));
            clientBuilder.proxy(proxy);
            if (webmagicProxy.getUsername() != null && webmagicProxy.getPassword() != null) {
                clientBuilder.proxyAuthenticator((route, response) -> {
                    String credential = okhttp3.Credentials.basic(webmagicProxy.getUsername(), webmagicProxy.getPassword());
                    return response.request().newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build();
                });
            }
            clientToUse = clientBuilder.build();
        }


        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder().url(request.getUrl());

        // Set headers from Site and Request
        if (site.getHeaders() != null) {
            for (Map.Entry<String, String> headerEntry : site.getHeaders().entrySet()) {
                requestBuilder.addHeader(headerEntry.getKey(), headerEntry.getValue());
            }
        }
        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                requestBuilder.addHeader(header.getKey(), header.getValue());
            }
        }
        // Set User-Agent
        if (site.getUserAgent() != null) {
            requestBuilder.header("User-Agent", site.getUserAgent());
        }

        // Handle request body for POST, PUT, etc.
        RequestBody okHttpRequestBody = null;
        if (request.getMethod() != null && !request.getMethod().equalsIgnoreCase(HttpConstant.Method.GET)) {
            if (request.getRequestBody() != null) {
                us.codecraft.webmagic.model.HttpRequestBody webmagicRequestBody = request.getRequestBody();
                okhttp3.MediaType mediaType = okhttp3.MediaType.parse(
                        webmagicRequestBody.getContentType() + "; charset=" + webmagicRequestBody.getEncoding()
                );
                okHttpRequestBody = RequestBody.create(webmagicRequestBody.getBody(), mediaType);
            } else if (request.getMethod().equalsIgnoreCase(HttpConstant.Method.POST)){
                // Handle empty POST body if necessary, OkHttp might require a default empty body
                okHttpRequestBody = RequestBody.create(new byte[0], null);
            }
        }
        requestBuilder.method(request.getMethod() == null ? HttpConstant.Method.GET : request.getMethod().toUpperCase(), okHttpRequestBody);


        // TODO: Implement cookie handling if site.isDisableCookieManagement() is false
        // This might involve using a custom CookieJar or manually adding cookie headers

        Page page = Page.ofFailure(request); // Initialize page as failure
        try (Response response = clientToUse.newCall(requestBuilder.build()).execute()) {
            page = handleResponse(request, site, response);
            onSuccess(page, task); // Call from AbstractDownloader
            return page;
        } catch (IOException e) {
            logger.warn("Download page {} error", request.getUrl(), e);
            onError(page, task, e); // Call from AbstractDownloader
            return page;
        } finally {
            if (proxyProvider != null && webmagicProxy != null) {
                proxyProvider.returnProxy(webmagicProxy, page, task);
            }
        }
    }

    protected Page handleResponse(Request request, Site site, Response response) throws IOException {
        Page page = Page.ofSuccess(request); // Now it's a success
        page.setStatusCode(response.code());
        page.setRequest(request);
        page.setUrl(new PlainText(request.getUrl()));

        if (responseHeader) {
            Map<String, List<String>> headersMap = new HashMap<>();
            response.headers().forEach(pair ->
                    headersMap.computeIfAbsent(pair.getFirst(), k -> new ArrayList<>()).add(pair.getSecond())
            );
            page.setHeaders(headersMap);
        }

        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            byte[] bytes = responseBody.bytes(); // Read bytes first
            page.setBytes(bytes);
            if (!request.isBinaryContent()) {
                String charset = request.getCharset();
                if (charset == null) {
                    okhttp3.MediaType contentType = responseBody.contentType();
                    if (contentType != null) {
                        charset = contentType.charset() != null ? contentType.charset().name() : null;
                    }
                    if (charset == null) {
                        charset = CharsetUtils.detectCharset(
                                contentType != null ? contentType.toString() : null,
                                bytes);
                    }
                    if (charset == null) {
                        charset = site.getCharset() != null ? site.getCharset() : Charset.defaultCharset().name();
                    }
                }
                page.setCharset(charset);
                page.setRawText(new String(bytes, charset));
            }
        }
        return page;
    }


    @Override
    public void setThread(int threadNum) {
        // OkHttp's dispatcher is typically configured globally or per client instance.
        // If specific adjustments are needed based on WebMagic's threadNum,
        // they would be made to the OkHttpClient's dispatcher configuration.
        // For simplicity, this can be a no-op or configured during OkHttpClient setup.
        logger.info("OkHttpDownloader's internal threading is managed by OkHttp's Dispatcher. " +
                "WebMagic's setThread({}) is noted but not directly applied to a specific pool size here.", threadNum);
    }

    @Override
    public void close() {
        // OkHttpClient has a connectionPool that can be evicted or closed.
        // And a dispatcher that can be shut down.
        if (okHttpClient != null) {
            okHttpClient.dispatcher().executorService().shutdown();
            okHttpClient.connectionPool().evictAll();
            // Note: Closing the cache would also be done here if one was configured.
            // okHttpClient.cache().close();
        }
    }
}