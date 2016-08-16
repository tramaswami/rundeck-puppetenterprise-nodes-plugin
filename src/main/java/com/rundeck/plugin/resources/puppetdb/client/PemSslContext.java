package com.rundeck.plugin.resources.puppetdb.client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.KeySpec;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.ssl.SSLContexts;

import com.puppetlabs.puppetdb.javaclient.ssl.KeySpecFactory;

public class PemSslContext {

    static private final char[] EMPTYPASSWORD = new char[] {};

    private final String host;
    private final String sslDir;
    private final SSLContext sslContext;
    private final KeyStore keyStore;

    public static SSLContext getContext(String host, String sslDir) throws IOException, GeneralSecurityException {
        return new PemSslContext(host, sslDir).sslContext;
    }

    private PemSslContext(String host, String sslDir) throws IOException, GeneralSecurityException {
        this.host = host;
        this.sslDir = sslDir;

        keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null);

        // initialize trust manager factory with the read truststore
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);

        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        loadCA(factory);
        loadPrivateCert(factory, EMPTYPASSWORD);

        sslContext = SSLContexts.custom()
                .loadKeyMaterial(keyStore, EMPTYPASSWORD)
                .loadTrustMaterial(keyStore, null)
                .build();

    }

    private Certificate generateCertificate(File certFile, CertificateFactory factory) throws CertificateException, IOException {
        try(InputStream input = new BufferedInputStream(new FileInputStream(certFile))) {
            return factory.generateCertificate(input);
        }
    }

    private Certificate getCACertificate(CertificateFactory factory) throws IOException, GeneralSecurityException {
        File caCertPEM = Paths.get(sslDir, "ca", "ca_crt.pem").toFile();
        return generateCertificate(caCertPEM, factory);
    }

    private Certificate getHostCertificate(CertificateFactory factory) throws IOException, GeneralSecurityException {
        File hostCertPEM = Paths.get(sslDir, "certs", host + ".pem").toFile();
        return generateCertificate(hostCertPEM, factory);
    }

    private void loadPrivateCert(CertificateFactory factory, char[] password) throws  IOException,
    GeneralSecurityException {
        keyStore.setKeyEntry(
                "key-alias", getPrivateKey(), password, new Certificate[] { getHostCertificate(factory) });
    }

    private PrivateKey getPrivateKey() throws IOException, GeneralSecurityException {
        File hostKeyPEM = Paths.get(sslDir, "private_keys", host + ".pem").toFile();
        KeySpec privateKey = KeySpecFactory.readKeySpec(hostKeyPEM);
        return KeyFactory.getInstance("RSA").generatePrivate(privateKey);
    }

    private void loadCA(CertificateFactory factory) throws IOException, GeneralSecurityException {
        // Set up a trustStore so that we can verify the server certificate
        Certificate caCert = getCACertificate(factory);
        if(caCert != null) {
            keyStore.setCertificateEntry("ca-cert-alias", caCert);
        }
    }

}
