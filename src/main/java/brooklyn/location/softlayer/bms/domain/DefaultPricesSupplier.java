package brooklyn.location.softlayer.bms.domain;

import java.util.Set;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;

public class DefaultPricesSupplier implements Supplier<Set<Price>> {

    @Override
    public Set<Price> get() {
        return ImmutableSet.of(
                new Price(33919), // Dual Processor Octo Core Xeon 2670 - 2.60GHz (Sandy Bridge) - 2 x 20MB cache
                new Price(25663), // CentOS 6.4 - Minimal Install (64 bit)
                new Price(22527), // 64 GB DDR3 Registered 1333
                new Price(32927), // Non-RAID
                new Price(31264), // 500GB SATA II
                new Price(24012), // 1.00TB SATA II
                new Price(26109), // 1 Gbps Redundant Public & Private Network Uplinks
                new Price(33867), // 20000 GB Bandwidth
                new Price(25014), // Reboot / KVM over IP
                new Price(34807), // 1 IP Address
                new Price(27023), // Host Ping
                new Price(32500), // Email and Ticket
                new Price(32627), // Automated Notification
                new Price(33483), // Unlimited SSL VPN Users & 1 PPTP VPN User per account
                new Price(35310)  // Nessus Vulnerability Assessment & Reporting
        );
    }
}
