package com.rundeck.plugin.resources.puppetdb.client;

import static java.lang.String.format;
import static java.util.Collections.emptyList;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.inject.Provider;
import com.puppetlabs.puppetdb.javaclient.impl.PEM_SSLSocketFactoryProvider;
import com.rundeck.plugin.resources.puppetdb.Constants;
import com.rundeck.plugin.resources.puppetdb.client.model.Fact;
import com.rundeck.plugin.resources.puppetdb.client.model.Node;
import com.rundeck.plugin.resources.puppetdb.client.model.NodeClass;
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
import org.apache.http.impl.conn.PoolingClientConnectionManager;
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

    public DefaultPuppetAPI(Properties properties) {
        puppetSslDir = properties.getProperty(PROPERTY_PUPPETDB_SSL_DIR);
        puppetProtocol = puppetSslDir == null ? "http" : HTTPS;
        puppetHost = properties.getProperty(PROPERTY_PUPPETDB_HOST);
        puppetPort = properties.getProperty(PROPERTY_PUPPETDB_PORT);
    }

    public String getBaseUrl(final String path) {
        final String baseUrl = format("%s://%s:%s/%s", puppetProtocol, puppetHost, puppetPort, path);
        LOG.info(format("baseUrl is: %s", baseUrl));
        return baseUrl;
    }

    @Override
    public List<Node> getNodes() {
        final CloseableHttpClient httpclient = puppetProtocol.equals(HTTPS) ? getHttpsClient() : new DefaultHttpClient();
        final HttpGet httpGet = new HttpGet(getBaseUrl("pdb/query/v4/nodes"));

        try (final CloseableHttpResponse response = httpclient.execute(httpGet)) {
            final int statusCode = response.getStatusLine().getStatusCode();
            final boolean ok = statusCode == HttpStatus.SC_OK;

            if (!ok) {
                LOG.warn(format("getNodes() ended with status code: %d msg: %s", statusCode, streamToString(response)));
                return emptyList();
            }

            final HttpEntity entity = response.getEntity();
            final String responseBody = EntityUtils.toString(entity);

            return GSON.fromJson(responseBody, Node.LIST);
        } catch (IOException e) {
            LOG.warn("while getNodes()", e);
        }

        return emptyList();
    }

    private static String streamToString(final CloseableHttpResponse response) throws IOException {
        final java.io.InputStream is = response.getEntity().getContent();
        try (java.util.Scanner s = new java.util.Scanner(is)) {
            return s.useDelimiter("\\A").hasNext() ? s.next() : "";
        }
    }

    @Override
    public List<NodeClass> getClassesForNode(final Node node) {
        final CloseableHttpClient httpclient = puppetProtocol.equals(HTTPS) ? getHttpsClient() : new DefaultHttpClient();
        final String url = format(getBaseUrl("pdb/query/v4/nodes/%s/resources/Class"), node.getCertname());
        final HttpGet httpGet = new HttpGet(url);

        try (final CloseableHttpResponse response = httpclient.execute(httpGet)) {
            final int statusCode = response.getStatusLine().getStatusCode();
            final boolean ok = statusCode == HttpStatus.SC_OK;

            if (!ok) {
                String statusMsg = streamToString(response);
                LOG.warn(format("getClasses(%s) ended with status code: %d msg: %s", node.getCertname(), statusCode, statusMsg));
                return emptyList();
            }

            final HttpEntity entity = response.getEntity();
            final String responseBody = EntityUtils.toString(entity);

            return GSON.fromJson(responseBody, NodeClass.LIST);
        } catch (IOException e) {
            LOG.warn("while getClasses()", e);
        }

        return emptyList();
    }

    @Override
    public List<Fact> getFactsForNode(final Node node) {
        final CloseableHttpClient httpclient = puppetProtocol.equals(HTTPS) ? getHttpsClient() : new DefaultHttpClient();
        final String url = format(getBaseUrl("pdb/query/v4/nodes/%s/facts"), node.getCertname());
        final HttpGet httpGet = new HttpGet(url);

        try (final CloseableHttpResponse response = httpclient.execute(httpGet)) {
            final int statusCode = response.getStatusLine().getStatusCode();
            final boolean ok = statusCode == HttpStatus.SC_OK;

            if (!ok) {
                LOG.warn(format("getFacts(%s) ended with status code: %d msg: %s", node.getCertname(), statusCode, streamToString(response)));
                return emptyList();
            }

            final HttpEntity entity = response.getEntity();
            final String responseBody = EntityUtils.toString(entity);

            return GSON.fromJson(responseBody, Fact.LIST);
        } catch (IOException e) {
            LOG.warn("while getFacts()", e);
        }

        return emptyList();
    }

    private DefaultHttpClient getHttpsClient() {
        Provider<SSLSocketFactory> provider = new PEM_SSLSocketFactoryProvider(puppetHost, Integer.parseInt(puppetPort), puppetSslDir);
        SSLSocketFactory sf = provider.get();

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme(HTTPS, 443, sf));

        ClientConnectionManager ccm = new PoolingClientConnectionManager(registry);

        return new DefaultHttpClient(ccm);
    }

}

