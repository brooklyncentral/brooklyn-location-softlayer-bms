package brooklyn.location.softlayer.bms;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;

import brooklyn.config.ConfigKey;
import brooklyn.entity.basic.ConfigKeys;
import brooklyn.location.jclouds.JcloudsLocationConfig;
import brooklyn.location.softlayer.bms.domain.Price;

public interface SoftlayerBmsLocationConfig extends JcloudsLocationConfig {

    public static final ConfigKey<Boolean> RUN_AS_ROOT = ConfigKeys.newBooleanConfigKey("runAsRoot",
            "Whether to run initial setup as root (default true)", null);
    public static final ConfigKey<String> DOMAIN = ConfigKeys.newStringConfigKey("domain",
            "Specify the domain for the FQDN", "ibm.com");
    public static final ConfigKey<Long> WAIT_FOR_ACTIVE = ConfigKeys.newLongConfigKey("waitForActive",
            "How many hours it waits for a server to be up and running", 12l);
    public static final ConfigKey<Set<Price>> PRICES = ConfigKeys.newConfigKey(new TypeToken<Set<Price>>() {
    },
            "prices", "hardware prices to place an order", ImmutableSet.<Price>of());
}
