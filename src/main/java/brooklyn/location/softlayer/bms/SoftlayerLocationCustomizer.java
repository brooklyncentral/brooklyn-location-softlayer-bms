package brooklyn.location.softlayer.bms;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.softlayer.compute.options.SoftLayerTemplateOptions;

import brooklyn.config.ConfigKey;
import brooklyn.entity.basic.ConfigKeys;
import brooklyn.location.jclouds.BasicJcloudsLocationCustomizer;
import brooklyn.location.jclouds.JcloudsLocation;
import brooklyn.util.text.Strings;

public class SoftlayerLocationCustomizer extends BasicJcloudsLocationCustomizer {
    public static final ConfigKey<Boolean> PRIVATE_NETWORK_ONLY = ConfigKeys.newBooleanConfigKey("privateNetworkOnly");
    public static final ConfigKey<Integer> VLAN_ID = ConfigKeys.newIntegerConfigKey("primaryNetworkComponentVlanId");
    public static final ConfigKey<Integer> BACKEND_VLAN_ID = ConfigKeys.newIntegerConfigKey("primaryBackendNetworkComponentVlanId");
    public static final ConfigKey<String> POST_INSTALL_SCRIPT_URI = ConfigKeys.newStringConfigKey("postInstallScriptUri");
    public static final ConfigKey<String> USER_DATA = ConfigKeys.newStringConfigKey("userData");

    @Override
    public void customize(JcloudsLocation location, ComputeService computeService, TemplateOptions templateOptions) {
        /*
        if (templateOptions instanceof SoftLayerTemplateOptions) {
            SoftLayerTemplateOptions options = (SoftLayerTemplateOptions)templateOptions;
            Boolean privateNetworkOnly = location.getConfig(PRIVATE_NETWORK_ONLY);
            if (privateNetworkOnly != null) {
                options.privateNetworkOnlyFlag(privateNetworkOnly);
            }

            Integer vlanId = location.getConfig(VLAN_ID);
            if (vlanId != null) {
                options.primaryNetworkComponentNetworkVlanId(vlanId);
            }

            Integer backendVlanId = location.getConfig(BACKEND_VLAN_ID);
            if (backendVlanId != null) {
                options.primaryBackendNetworkComponentNetworkVlanId(backendVlanId);
            }

            String postInstallScriptUri = location.getConfig(POST_INSTALL_SCRIPT_URI);
            if (!Strings.isEmpty(postInstallScriptUri)) {
                options.postInstallScriptUri(postInstallScriptUri);
            }

            String userData = location.getConfig(USER_DATA);
            if (!Strings.isEmpty(userData)) {
                options.userData(userData);
            }
        }
        */
    }
}
