/**
 * Copyright (c) 2013 Puppet Labs, Inc. and other contributors, as listed below.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Contributors: Puppet Labs, miguel@variacode.com
 *
 */
package com.puppetlabs.puppetdb.javaclient.impl;

import com.puppetlabs.puppetdb.javaclient.APIPreferences;
import com.puppetlabs.puppetdb.javaclient.BasicAPIPreferences;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.KeySpec;

import com.puppetlabs.puppetdb.javaclient.ssl.AbstractSSLSocketFactoryProvider;
import com.puppetlabs.puppetdb.javaclient.ssl.KeySpecFactory;

/**
 * Provides an SSLSocketFactory that has been configured according to the
 * settings in the injected {@link APIPreferences} where it will take the
 * following preferences into consideration:
 * <dl>
 * <dt>{@link APIPreferences#getPrivateKeyPEM() getPrivateKeyPEM()}</dt>
 * <dd>Mandatory. Used by the service used to authenticate this client.</dd>
 * <dt>{@link APIPreferences#getCertPEM() getCertPerm()}</dt>
 * <dd>Mandatory. Included in the certificate chain for the corresponding public
 * key.</dd>
 * <dt>{@link APIPreferences#getCaCertPEM() getCaCertPEM()}</dt>
 * <dd>Optional. If it is present, then the created factory will use a trust
 * store to validate the certificate. Otherwise it will allow self signed
 * certificates.</dd>
 * <dt>{@link APIPreferences#isAllowAllHosts()}</dt>
 * <dd>If <code>true</code>, then the created factory will disable hostname
 * verification.</dd>
 * </dl>
 */
public class PEM_SSLSocketFactoryProvider extends AbstractSSLSocketFactoryProvider {

    private final String host;
    private final int port;
    private final String sslDir;

    public PEM_SSLSocketFactoryProvider(String host, int port, String sslDir) {
        this.host = host;
        this.port = port;
        this.sslDir = sslDir;
    }

    private Certificate generateCertificate(File certFile, CertificateFactory factory) throws CertificateException, IOException {
        BufferedInputStream input = new BufferedInputStream(new FileInputStream(certFile));
        try {
            return factory.generateCertificate(input);
        } finally {
            input.close();
        }
    }

    @Override
    protected Certificate getCACertificate(CertificateFactory factory) throws IOException, GeneralSecurityException {
        File caCertPEM = getPreferences().getCaCertPEM();
        return caCertPEM == null
                ? null
                : generateCertificate(caCertPEM, factory);
    }

    @Override
    protected Certificate getHostCertificate(CertificateFactory factory) throws IOException, GeneralSecurityException {
        File hostCertPEM = getPreferences().getCertPEM();
        if (hostCertPEM == null) {
            throw new IOException("Missing required preferences setting for host certificate PEM file");
        }
        return generateCertificate(hostCertPEM, factory);
    }

    @Override
    protected KeySpec getPrivateKeySpec() throws KeyException, IOException {
        return KeySpecFactory.readKeySpec(getPreferences().getPrivateKeyPEM());
    }

    @Override
    protected APIPreferences getPreferences() {
        BasicAPIPreferences prefs = new BasicAPIPreferences();
        prefs.setServiceHostname(host);
        prefs.setServicePort(port);
        prefs.setAllowAllHosts(false);
        File caCertPem = new File(new File(sslDir, "ca"), "ca_crt.pem");
        if (caCertPem.canRead()) {
            prefs.setCaCertPEM(caCertPem);
        }
        prefs.setCertPEM(new File(new File(sslDir, "certs"), host + ".pem"));
        prefs.setPrivateKeyPEM(new File(new File(sslDir, "private_keys"), host + ".pem"));
        return prefs;
    }
}
