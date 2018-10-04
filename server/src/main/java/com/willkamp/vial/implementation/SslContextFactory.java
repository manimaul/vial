package com.willkamp.vial.implementation;

import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

class SslContextFactory {

    private final VialConfig vialConfig;
    private Logger log = LoggerFactory.getLogger(SslContextFactory.class);

    SslContextFactory(VialConfig vialConfig) {
        this.vialConfig = vialConfig;
    }

    @Nullable
    SslContext createSslContext() {
        if (vialConfig.isUseTls()) {

            try {
                SelfSignedCertificate cert = new SelfSignedCertificate();
                return SslContextBuilder
                        .forServer(cert.key(), cert.cert())
                        .sslProvider(SslProvider.OPENSSL)
                        /* NOTE: the cipher filter may not include all ciphers required by the HTTP/2 specification.
                         * Please refer to the HTTP/2 specification for cipher requirements.
                         */
                        .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                        .applicationProtocolConfig(new ApplicationProtocolConfig(
                                ApplicationProtocolConfig.Protocol.ALPN,
                                ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                                ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                                ApplicationProtocolNames.HTTP_2, ApplicationProtocolNames.HTTP_1_1))
                        .build();
            } catch (CertificateException | SSLException e) {
                log.error("error", e);
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }
}
