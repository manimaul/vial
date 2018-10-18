package com.willkamp;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.TlsVersion;

/**
 * Create okhttp clients using an unsafe trust manager for testing purposes.
 *
 * <p><strong>NOTE:</strong> Never use the objects created by this class in production. They are
 * purely for testing purposes, and thus very insecure.
 */
public class OkHttpUnsafe {

  private static X509TrustManager unsafeTrustManager() {
    return new X509TrustManager() {
      public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}

      public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}

      public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
      }
    };
  }

  private static SSLSocketFactory getUnsafeSSLSocketFactory(X509TrustManager trustManager)
      throws NoSuchAlgorithmException, KeyManagementException {
    final TrustManager[] trustAllCerts = new TrustManager[] {trustManager};
    final SSLContext sslContext = SSLContext.getInstance(TlsVersion.TLS_1_3.javaName());
    sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
    return sslContext.getSocketFactory();
  }

  public static OkHttpClient getUnsafeClient(Protocol... protocols) throws Exception {
    X509TrustManager trustManager = unsafeTrustManager();
    final SSLSocketFactory sslSocketFactory = getUnsafeSSLSocketFactory(trustManager);
    final List<Protocol> protocolList;
    if (protocols.length == 0) {
      protocolList = Collections.singletonList(Protocol.HTTP_1_1);
    } else {
      protocolList = Arrays.asList(protocols);
    }
    return new OkHttpClient.Builder()
        .sslSocketFactory(sslSocketFactory, trustManager)
        .hostnameVerifier((hostname, session) -> true)
        .protocols(protocolList)
        .build();
  }
}
