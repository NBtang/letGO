package com.letgo.core.internal.http;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.platform.Platform;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;

import static okhttp3.internal.platform.Platform.INFO;

public final class HttpLoggingInterceptor implements Interceptor {
    private static final Charset UTF8 = Charset.forName("UTF-8");

    public enum Level {
        /**
         * No logs.
         */
        NONE,
        /**
         * Logs request and response lines.
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1 (3-byte body)
         *
         * <-- 200 OK (22ms, 6-byte body)
         * }</pre>
         */
        BASIC,
        /**
         * Logs request and response lines and their respective headers.
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         * <-- END HTTP
         * }</pre>
         */
        HEADERS,
        /**
         * Logs request and response lines and their respective headers and bodies (if present).
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         *
         * Hi?
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         *
         * Hello!
         * <-- END HTTP
         * }</pre>
         */
        BODY
    }

    public interface Logger {
        void log(String url, String message);

        /**
         * A {@link Logger} defaults output appropriate for the current platform.
         */
        Logger DEFAULT = new Logger() {
            @Override
            public void log(String url, String message) {
                Platform.get().log(message, INFO, null);
            }
        };
    }

    public HttpLoggingInterceptor() {
        this(Logger.DEFAULT);
    }

    public HttpLoggingInterceptor(Logger logger) {
        this.logger = logger;
    }

    private final Logger logger;

    private volatile Level level = Level.NONE;

    /**
     * Change the level at which this interceptor logs.
     */
    public HttpLoggingInterceptor setLevel(Level level) {
        if (level == null) throw new NullPointerException("level == null. Use Level.NONE instead.");
        this.level = level;
        return this;
    }

    public Level getLevel() {
        return level;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Level level = this.level;

        StringBuilder logInfo = new StringBuilder();

        Request request = chain.request();
        if (level == Level.NONE) {
            return chain.proceed(request);
        }

        boolean logBody = level == Level.BODY;
        boolean logHeaders = logBody || level == Level.HEADERS;

        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;
        String url = request.url().toString();

        Connection connection = chain.connection();
        String requestStartMessage = "--> "
                + request.method()
                + ' ' + request.url()
                + (connection != null ? " " + connection.protocol() : "");
        if (!logHeaders && hasRequestBody) {
            requestStartMessage += " (" + requestBody.contentLength() + "-byte body)";
        }
        logInfo.append(requestStartMessage);

        if (logHeaders) {
            if (hasRequestBody) {
                // Request body headers are only present when installed as a network interceptor. Force
                // them to be included (when available) so there values are known.
                if (requestBody.contentType() != null) {
                    logInfo.append("\n");
                    logInfo.append("Content-Type: ").append(requestBody.contentType());
                }
                if (requestBody.contentLength() != -1) {
                    logInfo.append("\n");
                    logInfo.append("Content-Length: ").append(requestBody.contentLength());
                }
            }

            Headers headers = request.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                String name = headers.name(i);
                // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
                    logInfo.append("\n");
                    logInfo.append(name).append(": ").append(headers.value(i));
                }
            }

            if (!logBody || !hasRequestBody) {
                logInfo.append("\n");
                logInfo.append("--> END ").append(request.method());
            } else if (bodyHasUnknownEncoding(request.headers())) {
                logInfo.append("\n");
                logInfo.append("--> END ").append(request.method()).append(" (encoded body omitted)");
            } else {
                Buffer buffer = new Buffer();
                requestBody.writeTo(buffer);

                Charset charset = UTF8;
                MediaType contentType = requestBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }

                if (isPlaintext(buffer)) {
                    logInfo.append("\n");
                    String body = buffer.readString(charset);
                    try {
                        if (body.startsWith("{")) {
                            JSONObject jsonObject = new JSONObject(body);
                            String message = jsonObject.toString(2);
                            logInfo.append(message);
                        } else if (body.startsWith("[")) {
                            JSONArray jsonArray = new JSONArray(body);
                            String message = jsonArray.toString(2);
                            logInfo.append(message);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        logInfo.append(body);
                    }
                    logInfo.append("\n");
                    logInfo.append("--> END ").append(request.method()).append(" (").append(requestBody.contentLength()).append("-byte body)");
                } else {
                    logInfo.append("\n");
                    logInfo.append("--> END ").append(request.method()).append(" (binary ").append(requestBody.contentLength()).append("-byte body omitted)");
                }
            }
        }
        logger.log(url, logInfo.toString());
        logInfo.delete(0,logInfo.length());

        long startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            logInfo.append("\n");
            logInfo.append("<-- HTTP FAILED: ").append(e);
            throw e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        ResponseBody responseBody = response.body();
        long contentLength = responseBody.contentLength();
        String bodySize = contentLength != -1 ? contentLength + "-byte" : "unknown-length";
        logInfo.append("\n");
        logInfo.append("<-- ").append(response.code()).append(response.message().isEmpty() ? "" : ' ' + response.message()).append(' ').append(response.request().url()).append(" (").append(tookMs).append("ms").append(!logHeaders ? ", " + bodySize + " body" : "").append(')');

        if (logHeaders) {
            Headers headers = response.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                logInfo.append("\n");
                logInfo.append(headers.name(i)).append(": ").append(headers.value(i));
            }

            if (!logBody || !HttpHeaders.hasBody(response)) {
                logInfo.append("\n");
                logInfo.append("<-- END HTTP");
            } else if (bodyHasUnknownEncoding(response.headers())) {
                logInfo.append("\n");
                logInfo.append("<-- END HTTP (encoded body omitted)");
            } else {
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE); // Buffer the entire body.
                Buffer buffer = source.buffer();

                Long gzippedLength = null;
                if ("gzip".equalsIgnoreCase(headers.get("Content-Encoding"))) {
                    gzippedLength = buffer.size();
                    GzipSource gzippedResponseBody = null;
                    try {
                        gzippedResponseBody = new GzipSource(buffer.clone());
                        buffer = new Buffer();
                        buffer.writeAll(gzippedResponseBody);
                    } finally {
                        if (gzippedResponseBody != null) {
                            gzippedResponseBody.close();
                        }
                    }
                }

                Charset charset = UTF8;
                MediaType contentType = responseBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }

                if (!isPlaintext(buffer)) {
                    logInfo.append("\n");
                    logInfo.append("<-- END HTTP (binary ").append(buffer.size()).append("-byte body omitted)");
                    return response;
                }

                if (contentLength != 0) {
                    logInfo.append("\n");
                    String body = buffer.clone().readString(charset);
                    try {
                        if (body.startsWith("{")) {
                            JSONObject jsonObject = new JSONObject(body);
                            String message = jsonObject.toString(2);
                            logInfo.append(message);
                        } else if (body.startsWith("[")) {
                            JSONArray jsonArray = new JSONArray(body);
                            String message = jsonArray.toString(2);
                            logInfo.append(message);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        logInfo.append(body);
                    }
                }

                if (gzippedLength != null) {
                    logInfo.append("\n");
                    logInfo.append("<-- END HTTP (").append(buffer.size()).append("-byte, ").append(gzippedLength).append("-gzipped-byte body)");
                } else {
                    logInfo.append("\n");
                    logInfo.append("<-- END HTTP (").append(buffer.size()).append("-byte body)");
                }
            }
        }

        logger.log(url, logInfo.toString());

        return response;
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }

    private boolean bodyHasUnknownEncoding(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null
                && !contentEncoding.equalsIgnoreCase("identity")
                && !contentEncoding.equalsIgnoreCase("gzip");
    }
}
