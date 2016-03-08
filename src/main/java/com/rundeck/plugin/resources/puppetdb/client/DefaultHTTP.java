package com.rundeck.plugin.resources.puppetdb.client;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Function;
import com.google.inject.Provider;
import com.puppetlabs.puppetdb.javaclient.impl.PEM_SSLSocketFactoryProvider;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

import static java.lang.String.format;

/**
 * Use apache http client
 */
public class DefaultHTTP implements HTTP {
    private static final Logger LOG = Logger.getLogger(DefaultHTTP.class);
    private static final String HTTPS = "https";
    public static final String HTTP = "http";
    private final String puppetProtocol;
    private final String puppetHost;
    private final String puppetPort;
    private final String puppetSslDir;
    private final MetricRegistry metrics;

    public DefaultHTTP(
            final String puppetHost,
            final String puppetPort,
            final String puppetSslDir,
            final MetricRegistry metrics
    )
    {
        this.puppetProtocol = puppetSslDir == null ? HTTP : HTTPS;
        ;
        this.puppetHost = puppetHost;
        this.puppetPort = puppetPort;
        this.puppetSslDir = puppetSslDir;
        this.metrics = metrics;
    }

    public String getBaseUrl(final String path) {
        final String baseUrl = format("%s://%s:%s/%s", puppetProtocol, puppetHost, puppetPort, path);
        LOG.info(format("baseUrl is: %s", baseUrl));
        return baseUrl;
    }

    private HttpGet mkGet(final String path) {
        return new HttpGet(getBaseUrl(path));
    }

    @Override
    public <T> T makeRequest(
            final String path, Function<String, T> parser,
            T errResponse, final String name
    )
    {
        HttpGet get = mkGet(path);
        final CloseableHttpClient httpclient = getClient();

        LOG.debug(format("GET %s", get.getURI()));
        requestCounter();
        try (final CloseableHttpResponse response = httpclient.execute(get)) {
            final int statusCode = response.getStatusLine().getStatusCode();
            final boolean ok = statusCode == HttpStatus.SC_OK;

            if (!ok) {
                errorsCounter();
                LOG.warn(format(name + " ended with status code: %d msg: %s", statusCode, streamToString(response)));
                return errResponse;
            }

            final HttpEntity entity = response.getEntity();
            final String responseBody = EntityUtils.toString(entity);

            return parser.apply(responseBody);
        } catch (final IOException ex) {
            errorsCounter();
            LOG.warn(name + " exception while trying to request PuppetDB API: " + ex.getMessage(), ex);
        }
        return errResponse;
    }

    private static String streamToString(final CloseableHttpResponse response) throws IOException {
        final java.io.InputStream is = response.getEntity().getContent();
        try (java.util.Scanner s = new java.util.Scanner(is)) {
            return s.useDelimiter("\\A").hasNext() ? s.next() : "";
        }
    }

    CloseableHttpClient getClient() {
        return puppetProtocol.equalsIgnoreCase(HTTPS)
               ? getHttpsClient()
               : new DefaultHttpClient();
    }

    private CloseableHttpClient getHttpsClient() {
        return origGetHttpsClient();
    }

    private CloseableHttpClient origGetHttpsClient() {
        Provider<SSLSocketFactory> provider = new PEM_SSLSocketFactoryProvider(
                puppetHost,
                Integer.parseInt(puppetPort),
                puppetSslDir
        );
        SSLSocketFactory sf = provider.get();

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme(HTTPS, 443, sf));

        ClientConnectionManager ccm = new PoolingClientConnectionManager(registry);

        return new DefaultHttpClient(ccm);
    }

    private CloseableHttpClient getHttpsClient2() {
        HttpClientBuilder builder = HttpClientBuilder.create()
                                                     .setSSLSocketFactory(new PEM_SSLSocketFactoryProvider(
                                                             puppetHost,
                                                             Integer.parseInt(puppetPort),
                                                             puppetSslDir
                                                     ).get());
        builder.setSchemePortResolver(new DefaultSchemePortResolver());
        builder.setConnectionManager(new PoolingHttpClientConnectionManager());
        return builder.build();
    }

    private void errorsCounter() {
        metrics.counter(MetricRegistry.name(DefaultPuppetAPI.class, "http.errors.count")).inc();
    }

    private void requestCounter() {
        metrics.counter(MetricRegistry.name(DefaultPuppetAPI.class, "http.request.count")).inc();
    }

}
