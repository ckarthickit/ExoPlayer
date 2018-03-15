package com.karhick.android.kcextensions.stetho;

import com.facebook.stetho.inspector.network.DefaultResponseHandler;
import com.facebook.stetho.inspector.network.NetworkEventReporter;
import com.facebook.stetho.inspector.network.NetworkEventReporterImpl;
import com.facebook.stetho.inspector.network.RequestBodyHelper;
import com.facebook.stetho.urlconnection.SimpleRequestEntity;
import com.facebook.stetho.urlconnection.StethoURLConnectionManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by karthikc on 14/03/18.
 */

public class StethoUrlConnectionReporterImpl {
    private static final AtomicInteger sSequenceNumberGenerator = new AtomicInteger(0);

    private final NetworkEventReporter mStethoHook = NetworkEventReporterImpl.get();
    private final int mRequestId;
    @Nullable
    private final String mFriendlyName;

    private boolean hasInvokedPreConnect = false;
    private HttpURLConnection mConnection;

    @Nullable private URLConnectionInspectorRequest mInspectorRequest;
    @Nullable private RequestBodyHelper mRequestBodyHelper;

    public StethoUrlConnectionReporterImpl(@Nullable String friendlyName) {
        mRequestId = sSequenceNumberGenerator.getAndIncrement();
        mFriendlyName = friendlyName;
    }

    public boolean isStethoEnabled() {
        return mStethoHook.isEnabled();
    }

    /**
     * Indicates that the {@link HttpURLConnection} instance has been configured and is about
     * to be used to initiate an actual HTTP connection.  Call this method before any of the
     * active methods such as {@link HttpURLConnection#connect()},
     * {@link HttpURLConnection#getInputStream()}, or {@link HttpURLConnection#getOutputStream()}
     *
     * @param requestEntity Represents the request body if the request method supports it.
     */

    public void preConnect(
            /*Map<String,List<String>> requestProperties,
            String url,
            String method,*/
            HttpURLConnection connection,
            @Nullable SimpleRequestEntity requestEntity) {
        throwIfConnection();
        hasInvokedPreConnect = true;
        if (isStethoEnabled()) {
            mRequestBodyHelper = new RequestBodyHelper(mStethoHook, getStethoRequestId());
            mInspectorRequest = new URLConnectionInspectorRequest(
                    getStethoRequestId(),
                    mFriendlyName,
                    connection.getRequestProperties(),
                    connection.getURL().toString(),
                    connection.getRequestMethod(),
                    requestEntity,
                    mRequestBodyHelper);
            mStethoHook.requestWillBeSent(mInspectorRequest);
        }
    }


    /**
     * Indicates that the {@link HttpURLConnection} has just successfully exchanged HTTP messages
     * (request headers + body and response headers) with the server but has not yet consumed
     * the response body.
     *
     * @throws IOException May throw an exception internally due to {@link HttpURLConnection}
     *     method signatures.  The request should be considered aborted/failed if this method
     *     throws.
     */

    public void postConnect(final HttpURLConnection connection) throws IOException {
        mConnection = connection;
        throwIfNoConnection();
        if (isStethoEnabled()) {
            if (mRequestBodyHelper != null && mRequestBodyHelper.hasBody()) {
                mRequestBodyHelper.reportDataSent();
            }
            mStethoHook.responseHeadersReceived(
                    new URLConnectionInspectorResponse(
                            getStethoRequestId(),
                            mConnection));
        }
    }

    /**
     * Indicates that there was a non-recoverable failure during HTTP message exchange at some
     * point between {@link #preConnect} and {@link #interpretResponseStream}.
     *
     * @param ex Relay the exception that was thrown from {@link java.net.HttpURLConnection}
     */

    public void httpExchangeFailed(IOException ex) {
        throwIfNoConnection();
        if (isStethoEnabled()) {
            mStethoHook.httpExchangeFailed(getStethoRequestId(), ex.toString());
        }
    }


    /**
     * Deliver the response stream from {@link HttpURLConnection#getInputStream()} to
     * Stetho so that it can be intercepted.  Note that compression is transparently
     * supported on modern Android systems and no special awareness is necessary for
     * gzip compression on the wire.  Unfortunately this means that it is sometimes impossible
     * to determine whether compression actually occurred and so Stetho may report inflated
     * byte counts.
     * <p>
     * If the {@code Content-Length} header is provided by the server, this will be assumed to be
     * the raw byte count on the wire.
     *
     * @param responseStream Stream as furnished by {@link HttpURLConnection#getInputStream()}.
     *
     * @return The filtering stream which is to be read after this method is called.
     */

    public InputStream interpretResponseStream(@Nullable InputStream responseStream) {
        throwIfNoConnection();
        if (isStethoEnabled()) {
            // Note that Content-Encoding is stripped out by HttpURLConnection on modern versions of
            // Android (fun fact, it's powered by okhttp) when decompression is handled transparently.
            // When this occurs, we will not be able to report the compressed size properly.  Callers,
            // however, can disable this behaviour which will once again give us access to the raw
            // Content-Encoding so that we can handle it properly.
            responseStream = mStethoHook.interpretResponseStream(
                    getStethoRequestId(),
                    mConnection.getHeaderField("Content-Type"),
                    mConnection.getHeaderField("Content-Encoding"),
                    responseStream,
                    new DefaultResponseHandler(mStethoHook, getStethoRequestId()));
        }
        return responseStream;
    }

    private void throwIfConnection() {
        if (hasInvokedPreConnect) {
            throw new IllegalStateException("Must not call preConnect twice");
        }
    }

    private void throwIfNoConnection() {
        if(!hasInvokedPreConnect || mConnection == null) {
            throw new IllegalStateException("Must call preConnect");
        }
    }

    /**
     * @see StethoUrlConnectionReporterImpl#getStethoRequestId()
     */
    @Nonnull
    public String getStethoRequestId() {
        return String.valueOf(mRequestId);
    }
}
