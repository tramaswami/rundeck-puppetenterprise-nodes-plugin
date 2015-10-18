package com.rundeck.plugin.resources.puppetdb.client;

import static java.lang.String.format;
import static java.util.Collections.emptyList;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.google.gson.Gson;
import com.rundeck.plugin.resources.puppetdb.Constants;
import com.rundeck.plugin.resources.puppetdb.client.model.Fact;
import com.rundeck.plugin.resources.puppetdb.client.model.Node;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class DefaultPuppetAPI implements PuppetAPI, Constants {

    private static Logger LOG = Logger.getLogger(DefaultPuppetAPI.class);
    private static final Gson GSON = new Gson();

    private final String puppetProtocol;
    private final String puppetHost;
    private final String puppetPort;

    public DefaultPuppetAPI(Properties properties) {
        puppetProtocol = "http"; // TODO puppetProtocol as parameter too?
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
        final CloseableHttpClient httpclient = new DefaultHttpClient();
        final HttpGet httpGet = new HttpGet(getBaseUrl("pdb/query/v4/nodes"));

        try (final CloseableHttpResponse response = httpclient.execute(httpGet)) {
            final int statusCode = response.getStatusLine().getStatusCode();
            final boolean ok = statusCode == HttpStatus.SC_OK;

            if (!ok) {
                LOG.warn(format("getNodes() ended with status code: %d", statusCode));
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

    @Override
    public List<Fact> getFactsForNode(final Node node) {
        final CloseableHttpClient httpclient = new DefaultHttpClient();
        final String url = format(getBaseUrl("pdb/query/v4/nodes/%s/facts"), node.getCertname());
        final HttpGet httpGet = new HttpGet(url);

        try (final CloseableHttpResponse response = httpclient.execute(httpGet)) {
            final int statusCode = response.getStatusLine().getStatusCode();
            final boolean ok = statusCode == HttpStatus.SC_OK;

            if (!ok) {
                LOG.warn(format("getFacts(%s) ended with status code: %d",
                        node.getCertname(),
                        statusCode));
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


}
