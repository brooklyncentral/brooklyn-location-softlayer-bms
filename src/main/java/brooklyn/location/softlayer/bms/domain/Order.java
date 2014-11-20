package brooklyn.location.softlayer.bms.domain;

import static com.google.common.base.Preconditions.checkNotNull;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

public class Order {
    private String complexType = "SoftLayer_Container_Product_Order_Hardware_Server";
    private Set<Hardware> hardware;
    private String location;
    private Set<Price> prices;
    private long packageId = -1;

    public Order(Set<Hardware> hardware, String location, Set<Price> prices, long packageId) {
        this.hardware = hardware;
        this.location = location;
        this.prices = prices;
        this.packageId = packageId;
    }

    public Set<Hardware> getHardware() {
        return hardware;
    }

    public String getLocation() {
        return location;
    }

    public Set<Price> getPrices() {
        return prices;
    }

    public long getPackageId() {
        return packageId;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("complexType", complexType)
                .add("hardware", hardware)
                .add("location", location)
                .add("prices", prices)
                .add("packageId", packageId)
                .toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return builder().fromOrder(this);
    }

    public static final class Builder {
        private Set<Hardware> hardware;
        private String location;
        private Set<Price> prices;
        private long packageId = -1;

        public Builder packageId(long packageId) {
            this.packageId = packageId;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder prices(Set<Price> prices) {
            this.prices = ImmutableSet.copyOf(checkNotNull(prices, "prices"));
            return this;
        }

        public Builder hardware(Set<Hardware> hardware) {
            this.hardware = ImmutableSet.copyOf(checkNotNull(hardware, "hardware"));
            return this;
        }

        public Order build() {
            return new Order(hardware, location, prices, packageId);
        }

        public Builder fromOrder(Order in) {
            return this
                    .hardware(in.getHardware())
                    .location(in.getLocation())
                    .prices(in.getPrices())
                    .packageId(in.getPackageId());
        }
    }
}
