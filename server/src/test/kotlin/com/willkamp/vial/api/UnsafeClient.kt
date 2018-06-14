package com.willkamp.vial.api

import okhttp3.OkHttpClient
import okhttp3.Protocol
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import javax.net.ssl.*

object UnsafeClient {

    private fun unsafeTrustManager(): X509TrustManager {
        return object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                return emptyArray()
            }
        }
    }

    @Throws(NoSuchAlgorithmException::class, KeyManagementException::class)
    private fun getUnsafeSSLSocketFactory(
            keyManagers: Array<KeyManager>?, trustManager: X509TrustManager): SSLSocketFactory {
        // Create a trust manager that does not validate certificate chains
        val trustAllCerts = arrayOf<TrustManager>(trustManager)

        // Install the all-trusting trust manager
        val sslContext = SSLContext.getInstance("TLSv1.2")
        sslContext.init(keyManagers, trustAllCerts, java.security.SecureRandom())
        return sslContext.socketFactory
    }

    @Throws(Exception::class)
    fun getUnsafeClient(h2: Boolean): OkHttpClient {
        val trustManager = unsafeTrustManager()
        val sslSocketFactory = getUnsafeSSLSocketFactory(null, trustManager)
        val protocolList: List<Protocol> = if (h2) {
            listOf(Protocol.HTTP_2, Protocol.HTTP_1_1)

        } else {
            listOf(Protocol.HTTP_1_1)
        }
        return OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustManager)
                .hostnameVerifier { _, _ -> true }
                .protocols(protocolList)
                .build()
    }
}