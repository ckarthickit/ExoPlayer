package com.karhick.android.kcextensions;

import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Predicate;

/**
 * Created by karthikc on 14/03/18.
 */

public class StethoHttpDataSource extends DefaultHttpDataSource {

    public StethoHttpDataSource(String userAgent, Predicate<String> contentTypePredicate,
                                 TransferListener<? super DefaultHttpDataSource> listener, int connectTimeoutMillis,
                                 int readTimeoutMillis, boolean allowCrossProtocolRedirects,
                                 RequestProperties defaultRequestProperties) {
        super(userAgent,contentTypePredicate,listener,connectTimeoutMillis,readTimeoutMillis,allowCrossProtocolRedirects,defaultRequestProperties);
    }
}
