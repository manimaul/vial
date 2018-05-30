package com.willkamp.vial.api

import io.netty.handler.ssl.util.SelfSignedCertificate
import java.security.PrivateKey
import java.security.cert.X509Certificate

class TlsContext(
        val privateKey: PrivateKey,
        val keyCertChain: Array<out X509Certificate>
) {
    companion object {
        fun forServerSelfSigned(): TlsContext {
            val ssc = SelfSignedCertificate()
            return TlsContext(ssc.key(), arrayOf(ssc.cert()))
        }

        fun forServer(key: PrivateKey, vararg keyCertChain: X509Certificate): TlsContext {
            return TlsContext(key, keyCertChain)
        }
    }
}