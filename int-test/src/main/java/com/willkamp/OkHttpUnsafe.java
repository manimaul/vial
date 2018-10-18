package com.willkamp;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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

  public static OkHttpClient getUnsafeClient(String... protocols) throws Exception {
    List<Protocol> protocolList =
        Arrays.stream(protocols)
            .map(
                it -> {
                  try {
                    return Protocol.get(it);
                  } catch (IOException e) {
                    return null;
                  }
                })
            .collect(Collectors.toList());
    return getUnsafeClient(protocolList);
  }

  public static OkHttpClient getUnsafeClient(Protocol... protocols) throws Exception {
    return getUnsafeClient(Arrays.asList(protocols));
  }

  public static OkHttpClient getUnsafeClient(List<Protocol> protocols) throws Exception {
    X509TrustManager trustManager = unsafeTrustManager();
    final SSLSocketFactory sslSocketFactory = getUnsafeSSLSocketFactory(trustManager);
    if (protocols.isEmpty()) {
      throw new RuntimeException("protocols required");
    }
    return new OkHttpClient.Builder()
        .sslSocketFactory(sslSocketFactory, trustManager)
        .hostnameVerifier((hostname, session) -> true)
        .protocols(protocols)
        .build();
  }
}
