package brooklyn.location.softlayer.bms.domain;

public class Hardware {
    private String hostname;
    private String domain;
    private boolean bareMetalInstanceFlag;

    public Hardware(String hostname, String domain, boolean bareMetalInstanceFlag) {
        this.hostname = hostname;
        this.domain = domain;
        this.bareMetalInstanceFlag = bareMetalInstanceFlag;
    }
}