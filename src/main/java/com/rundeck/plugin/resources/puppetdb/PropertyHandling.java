package com.rundeck.plugin.resources.puppetdb;


import static com.google.common.base.Optional.absent;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.dtolabs.rundeck.core.plugins.configuration.Property;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import org.apache.log4j.Logger;

/**
 * kind of DSL for resolving properties
 */
public final class PropertyHandling implements Constants {

    private static Logger LOG = Logger.getLogger(PropertyHandling.class);

    private PropertyHandling() {}

    public static Optional<String> readPuppetDbQuery(final Properties properties) {
        try {
            if (properties.getProperty(PROPERTY_NODE_QUERY) == null) {
                return absent();
            }

            final String query = URLEncoder.encode(properties.getProperty(PROPERTY_NODE_QUERY), java.nio.charset.StandardCharsets.UTF_8.toString());
            return Optional.of(query);
        } catch (UnsupportedEncodingException ex) {
            LOG.warn(format("Error while reading property: %s", getPropertyTitle(PROPERTY_NODE_QUERY, PROPERTY_NODE_QUERY)), ex);
        }

        return absent();
    }

    public static String getPropertyTitle(final String propertyName, final String defaultTitle) {
        final Function<Property, String> getTitle = new Function<Property, String>() {
            @Override
            public String apply(final Property input) {
                return input == null ? "" : input.getTitle();
            }
        };

        final String value = getProperty(propertyName, getTitle).orNull();
        final boolean isEmpty = value == null || value.trim().isEmpty();

        return isEmpty ? defaultTitle : value;
    }

    public static <K> Optional<K> getProperty(final String propertyName, final Function<Property, K> andThen) {
        final List<Property> properties = PLUGIN_DESCRIPTION.getProperties();

        final Predicate<Property> equalsPropertyName = new Predicate<Property>() {
            @Override
            public boolean apply(final Property input) {
                return input != null && propertyName.equals(input.getName());
            }
        };

        return FluentIterable
                .from(properties)
                .filter(equalsPropertyName)
                .transform(andThen)
                .first();
    }

    public static String joinPropertyNames(final List<Property> properties) {
        final Function<Property, String> getName = new Function<Property, String>() {
            @Override
            public String apply(final Property input) {
                return input.getName();
            }
        };

        return FluentIterable.from(properties)
                .transform(getName)
                .join(Joiner.on(","));
    }

    public static List<Property> getMissingProperties(final Properties properties) {
        final List<Property> result = new ArrayList<>();

        for (final Property property : PLUGIN_DESCRIPTION.getProperties()) {
            if (!property.isRequired()) {
                continue;
            }

            final String key = property.getName();
            final String value = properties.getProperty(key, "");

            final boolean isMissing = isBlank(value);
            if (isMissing) {
                result.add(property);
            }

        }

        return result;
    }


}
