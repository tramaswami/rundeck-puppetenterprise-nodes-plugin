package com.rundeck.plugin.resources.puppetdb.client;

import static java.lang.String.format;

import java.io.IOException;
import java.util.*;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.inject.Provider;
import com.puppetlabs.puppetdb.javaclient.impl.PEM_SSLSocketFactoryProvider;
import com.rundeck.plugin.resources.puppetdb.Constants;
import com.rundeck.plugin.resources.puppetdb.PropertyHandling;
import com.rundeck.plugin.resources.puppetdb.client.model.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
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

public class DefaultPuppetAPI extends PuppetAPI implements Constants {

    private static final String HTTPS = "https";

    private static final Logger LOG = Logger.getLogger(DefaultPuppetAPI.class);
    private static final Gson GSON = new Gson();

    private final String puppetProtocol;
    private final String puppetHost;
    private final String puppetPort;
    private final String puppetSslDir;
    private final String puppetNodeQuery;
    private final MetricRegistry metrics;

    public DefaultPuppetAPI(final Properties properties, final MetricRegistry metrics) {
        this.puppetSslDir = properties.getProperty(PROPERTY_PUPPETDB_SSL_DIR);
        this.puppetProtocol = puppetSslDir == null ? "http" : HTTPS;
        this.puppetHost = properties.getProperty(PROPERTY_PUPPETDB_HOST);
        this.puppetPort = properties.getProperty(PROPERTY_PUPPETDB_PORT);
        this.puppetNodeQuery = PropertyHandling.readPuppetDbQuery(properties).orNull();
        this.metrics = metrics;
    }

    public String getBaseUrl(final String path) {
        final String baseUrl = format("%s://%s:%s/%s", puppetProtocol, puppetHost, puppetPort, path);
        LOG.info(format("baseUrl is: %s", baseUrl));
        return baseUrl;
    }

    @Override
    public List<Node> getNodes() {
        String url = "pdb/query/v4/nodes" + getUserQuery();
        final HttpGet httpGet = new HttpGet(getBaseUrl(url));
        return makeRequest(httpGet, Node.listParser(GSON), Collections.<Node>emptyList(), "getNodes()");
    }

    Function<String, List<NodeFact>> singleFact() {
        return new Function<String, List<NodeFact>>() {
            @Override
            public List<NodeFact> apply(final String input) {
                return getFactForAllNodes(input);
            }
        };
    }

    public List<NodeFact> getFactSet(Set<String> facts) {
        ImmutableList<List<NodeFact>> lists = FluentIterable.from(facts)
                                                            .transform(
                singleFact()
        ).toList();
        List<NodeFact> nodeFacts = new ArrayList<>();
        for (List<NodeFact> list : lists) {
            nodeFacts.addAll(list);
        }
        return nodeFacts;
    }

    public List<NodeFact> getFactForAllNodes(String fact) {
        String path = "pdb/query/v4/facts/%s"+ getUserQuery();
        String url = format(path, fact);
        final HttpGet httpGet = new HttpGet(getBaseUrl(url));

        return makeRequest(httpGet, NodeFact.parser(GSON), Collections.<NodeFact>emptyList(), "getFactForAllNodes()");
    }

    private String getUserQuery() {
        return (puppetNodeQuery != null && !puppetNodeQuery.trim().isEmpty())
         ? ("?query=") + puppetNodeQuery
         : "";
    }

    public <T> T makeRequest(
            final HttpUriRequest httpGet,
            Function<String, T> parser,
            T errResponse, final String name
    ){
        final CloseableHttpClient httpclient = puppetProtocol.equals(HTTPS)
                                               ? getHttpsClient()
                                               : new DefaultHttpClient();

        LOG.debug(format("GET %s", httpGet.getURI()));
        requestCounter();
        try (final CloseableHttpResponse response = httpclient.execute(httpGet)) {
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
            LOG.warn(name + " exception while trying to request PuppetDB API: "+ex.getMessage(), ex);
        }
        return errResponse;
    }

    private void errorsCounter() {
        metrics.counter(MetricRegistry.name(DefaultPuppetAPI.class, "http.errors.count")).inc();
    }
    private void requestCounter() {
        metrics.counter(MetricRegistry.name(DefaultPuppetAPI.class, "http.request.count")).inc();
    }

    private static String streamToString(final CloseableHttpResponse response) throws IOException {
        final java.io.InputStream is = response.getEntity().getContent();
        try (java.util.Scanner s = new java.util.Scanner(is)) {
            return s.useDelimiter("\\A").hasNext() ? s.next() : "";
        }
    }

    @Override
    public List<NodeClass> getClassesForNode(final Node node) {
        final String url = format(getBaseUrl("pdb/query/v4/nodes/%s/resources/Class"), node.getCertname());
        final HttpGet httpGet = new HttpGet(url);
        return makeRequest(httpGet, NodeClass.parser(GSON), Collections.<NodeClass>emptyList(), "getClassesForNode()");
    }

    public List<CertNodeClass> getClassesForAllNodes() {
        String path = "pdb/query/v4/resources/Class" + getUserQuery();
        final String url = getBaseUrl(path);
        final HttpGet httpGet = new HttpGet(url);
        return makeRequest(httpGet, CertNodeClass.listParser(GSON), Collections.<CertNodeClass>emptyList(), "getClassesForAllNodes()");
    }

    @Override
    public List<Fact> getFactsForNode(final Node node) {
        final String url = format(getBaseUrl("pdb/query/v4/nodes/%s/facts"), node.getCertname());
        final HttpGet httpGet = new HttpGet(url);
        return makeRequest(httpGet, Fact.listParser(GSON), Collections.<Fact>emptyList(), "getFactsForNode()");
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

}
