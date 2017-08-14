package com.rundeck.plugin.resources.puppetdb;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Optional;

/**
 * Created by greg on 3/7/16.
 */
public class TestDetermineFactNames {
    private final String resourcePath = "simple";

    private final Mapper mapper = new Mapper(Optional.<String>absent());

    Map<String, Object> mapping;

    @Test
    public void test_known_mapping() {
        this.mapping = TestUtilities.getMapping(resourcePath + "/known_mapping.json");
        Set<String> strings = mapper.determineFactNames(mapping);
        Object[] a = strings.toArray();
        Arrays.sort(a);
        Assert.assertArrayEquals(
                new String[]{"architecture","hardwareisa", "ipaddress", "kernelversion","os"},
                a
        );
    }

}
