package brooklyn.location.softlayer.bms.domain;

import static com.google.common.base.Preconditions.checkNotNull;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;

public class BareMetalOrderSupplier implements Supplier<Order> {

    private final String hostname;
    private final String domain;
    private final boolean bareMetalInstanceFlag;
    private final String location;
    private final Set<Price> prices;

    public BareMetalOrderSupplier(String hostname, String domain, boolean bareMetalInstanceFlag, String location, Set<Price> prices) {
        this.hostname = checkNotNull(hostname, "hostname");
        this.domain = checkNotNull(domain, "domain");
        this.bareMetalInstanceFlag = checkNotNull(bareMetalInstanceFlag, "bareMetalInstanceFlag");
        this.location = checkNotNull(location, "location");
        this.prices = ImmutableSet.copyOf(prices);
    }

    @Override
    public Order get() {
        return Order.builder()
                .hardware(ImmutableSet.of(new Hardware(hostname, domain, bareMetalInstanceFlag)))
                .location(location)
                .packageId(143)
                .prices(prices.isEmpty() ? new DefaultPricesSupplier().get() : prices)
                .build();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("hostname", hostname)
                .add("domain", domain)
                .add("bareMetalInstanceFlag", bareMetalInstanceFlag)
                .add("location", location)
                .add("prices", prices)
                .toString();
    }
}
