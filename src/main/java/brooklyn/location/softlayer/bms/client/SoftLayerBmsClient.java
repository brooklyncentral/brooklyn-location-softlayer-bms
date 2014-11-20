package brooklyn.location.softlayer.bms.client;

import static java.lang.String.format;
import java.net.URI;
import java.util.Map;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import brooklyn.location.softlayer.bms.domain.Order;
import brooklyn.util.collections.Jsonya;
import brooklyn.util.http.HttpTool;
import brooklyn.util.http.HttpToolResponse;
import brooklyn.util.net.Urls;

public class SoftLayerBmsClient {

    private static final String SL_BASE_URI = "https://api.softlayer.com/rest/v3/";
    private static final Map<String, String> EMPTY_HEADERS = ImmutableMap.of();

    private final HttpClient client;

    public SoftLayerBmsClient(String identity, String credential) {
        this.client = HttpTool.httpClientBuilder().uri(SL_BASE_URI)
                                                  .credentials(new UsernamePasswordCredentials(identity, credential))
                                                  .build();
    }

    public Map<Object, Object> placeOrder(Order order) {
        URI orderProductUri = URI.create(Urls.mergePaths(SL_BASE_URI.toString(), "SoftLayer_Product_Order/placeOrder"));
        String orderJson = new Gson().toJson(ImmutableMap.of("parameters", ImmutableList.of(order)));
        HttpToolResponse orderProductResponse = HttpTool.httpPost(client, orderProductUri, EMPTY_HEADERS, orderJson.getBytes());
        return getResponseAsMap(orderProductResponse);
    }

    public Map<Object, Object> getObject(String globalIdentifier) {
        final URI getObjectURI = createGetObjectUri(globalIdentifier);
        HttpToolResponse getStatusResponse = HttpTool.httpGet(client, getObjectURI, EMPTY_HEADERS);
        return getResponseAsMap(getStatusResponse);
    }

    public String getHardwareStatus(String hardwareId) {
        URI getHardwareStatusURI = createGetHardwareStatusUri(hardwareId);
        HttpToolResponse getStatusResponse = HttpTool.httpGet(client, getHardwareStatusURI, EMPTY_HEADERS);
        Map result = getResponseAsMap(getStatusResponse);
        return Jsonya.of(result).get("status").toString();
    }

    public boolean deleteServer(String globalIdentifier) {
        final URI getObjectURI = createGetObjectUri(globalIdentifier);
        HttpToolResponse getStatusResponse = HttpTool.httpGet(client, getObjectURI, EMPTY_HEADERS);
        Map<Object, Object> getStatusMap = getResponseAsMap(getStatusResponse);
        int hardwareId = ((Double) Jsonya.of(getStatusMap).get("id")).intValue();
        return cancelServerTicketUri(client, createCreateCancelServerTicketUri(), hardwareId).equals("CANCELLED");
    }

    private String cancelServerTicketUri(HttpClient client, URI cancelServerTicketUri, int hardwareId) {
        CancelServerRequest cancelServerRequest = createCancelServerRequest(hardwareId);
        String cancelServerTicketJson = new Gson().toJson(cancelServerRequest);
        //ImmutableMap.of("parameters", ImmutableList.of(cancelServerRequest)));
        HttpToolResponse getStatusResponse = HttpTool.httpPost(client, cancelServerTicketUri, EMPTY_HEADERS, cancelServerTicketJson.getBytes());
        Map<Object, Object> cancelServerTicketMap = getResponseAsMap(getStatusResponse);
        return Jsonya.of(cancelServerTicketMap).get("status").toString();
    }

    public boolean powerOn(String globalIdentifier) {
        URI powerOnURI = createPowerOnUri(globalIdentifier);
        HttpToolResponse powerOnResponse = HttpTool.httpGet(client, powerOnURI, EMPTY_HEADERS);
        return getResponseAsBoolean(powerOnResponse);
    }

    public boolean powerOff(String globalIdentifier) {
        URI powerOffURI = createPowerOffUri(globalIdentifier);
        HttpToolResponse powerOffResponse = HttpTool.httpGet(client, powerOffURI, EMPTY_HEADERS);
        return getResponseAsBoolean(powerOffResponse);
    }

    public boolean rebootSoft(String globalIdentifier) {
        URI rebootSoftURI = createRebootSoftUri(globalIdentifier);
        HttpToolResponse rebootSoftResponse = HttpTool.httpGet(client, rebootSoftURI, EMPTY_HEADERS);
        return getResponseAsBoolean(rebootSoftResponse);
    }

    public boolean rebootHard(String globalIdentifier) {
        URI rebootHardURI = createRebootSoftUri(globalIdentifier);
        HttpToolResponse rebootHardResponse = HttpTool.httpGet(client, rebootHardURI, EMPTY_HEADERS);
        return getResponseAsBoolean(rebootHardResponse);
    }

    private Map getResponseAsMap(HttpToolResponse response) {
        return new Gson().fromJson(new String(response.getContent()), Map.class);
    }

    private boolean getResponseAsBoolean(HttpToolResponse response) {
        return new Gson().fromJson(new String(response.getContent()), Boolean.class);
    }

    private URI createGetHardwareStatusUri(String globalIdentifier) {
        return createUri("SoftLayer_Hardware/%s/getHardwareStatus", globalIdentifier);
    }

    private URI createGetObjectUri(String globalIdentifier) {
        return createUri("SoftLayer_Hardware/%s/getObject?objectMask=operatingSystem.passwords;billingItem",
                globalIdentifier);
    }

    private URI createPowerOnUri(String globalIdentifier) {
        return createUri("SoftLayer_Hardware/%s/powerOn", globalIdentifier);
    }

    private URI createPowerOffUri(String globalIdentifier) {
        return createUri("SoftLayer_Hardware/%s/powerOff", globalIdentifier);
    }

    private URI createRebootSoftUri(String globalIdentifier) {
        return createUri("SoftLayer_Hardware/%s/rebootSoft", globalIdentifier);
    }

    private URI createRebootHardUri(String globalIdentifier) {
        return createUri("SoftLayer_Hardware/%s/rebootHard", globalIdentifier);
    }

    private URI createUri(String format, String ... args) {
        return URI.create(Urls.mergePaths(SL_BASE_URI.toString(), format(format, args)));
    }

    private CancelServerRequest createCancelServerRequest(int attachmentId) {
        return CancelServerRequest.builder().attachmentId(attachmentId)
                                            .reason("No longer needed")
                                            .cancelAssociatedItems(true)
                                            .attachmentType("HARDWARE")
                                            .build();
    }

    public URI createCreateCancelServerTicketUri() {
        return URI.create(Urls.mergePaths(SL_BASE_URI.toString(),
                "SoftLayer_Ticket/createCancelServerTicket"));
    }

    public String createCancelServerTicket(int hardwareId) {
        URI cancelServerTicketUri = createCreateCancelServerTicketUri();
        CancelServerRequest cancelServerRequest = createCancelServerRequest(hardwareId);
        String cancelServerTicketJson = new Gson().toJson(cancelServerRequest);
        //ImmutableMap.of("parameters", ImmutableList.of(cancelServerRequest)));
        HttpToolResponse getStatusResponse = HttpTool.httpPost(client, cancelServerTicketUri, EMPTY_HEADERS, cancelServerTicketJson.getBytes());
        Map<Object, Object> cancelServerTicketMap = getResponseAsMap(getStatusResponse);
        return Jsonya.of(cancelServerTicketMap).get("status").toString();
    }
}
