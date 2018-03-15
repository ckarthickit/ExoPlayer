package com.karhick.android.kcextensions.stetho;

import com.facebook.stetho.inspector.network.NetworkEventReporter;
import com.facebook.stetho.inspector.network.RequestBodyHelper;
import com.facebook.stetho.urlconnection.SimpleRequestEntity;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Created by karthikc on 15/03/18.
 */

public class URLConnectionInspectorRequest
        extends com.karhick.android.kcextensions.stetho.URLConnectionInspectorHeaders
        implements NetworkEventReporter.InspectorRequest {
    private final String mRequestId;
    private final String mFriendlyName;
    @Nullable
    private final SimpleRequestEntity mRequestEntity;
    private final RequestBodyHelper mRequestBodyHelper;
    private final String mUrl;
    private final String mMethod;

    public URLConnectionInspectorRequest(
            String requestId,
            String friendlyName,
            Map<String, List<String>> requestProperties,
            String url,
            String method,
            @Nullable SimpleRequestEntity requestEntity,
            RequestBodyHelper requestBodyHelper) {
        super(Util.convertHeaders(requestProperties));
        mRequestId = requestId;
        mFriendlyName = friendlyName;
        mRequestEntity = requestEntity;
        mRequestBodyHelper = requestBodyHelper;
        mUrl = url;
        mMethod = method;
    }

    @Override
    public String id() {
        return mRequestId;
    }

    @Override
    public String friendlyName() {
        return mFriendlyName;
    }

    @Override
    public Integer friendlyNameExtra() {
        return null;
    }

    @Override
    public String url() {
        return mUrl;
    }

    @Override
    public String method() {
        return mMethod;
    }

    @Nullable
    @Override
    public byte[] body() throws IOException {
        if (mRequestEntity != null) {
            OutputStream out = mRequestBodyHelper.createBodySink(firstHeaderValue("Content-Encoding"));
            try {
                mRequestEntity.writeTo(out);
            } finally {
                out.close();
            }
            return mRequestBodyHelper.getDisplayBody();
        } else {
            return null;
        }
    }
}
