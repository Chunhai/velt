package org.onosproject.velt;



import com.fasterxml.jackson.databind.JsonNode;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.config.Config;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Configuration object for CordVtn service.
 * Created by Chunhai on 8/11/16.
 * All rights reserved.
 */
public class VeltConfig extends Config<ApplicationId> {

    protected final Logger log = getLogger(getClass());

    private static final String BRIDGEID = "bridgeId";
    private static final String DATAPLANEPORT = "dataPlanePort";
    private static final String VMPORT1 = "vmPort1";
    private static final String VMPORT2 = "vmPort2";
    private static final String SITEVLAN = "siteVlan";
    private static final String TEMPVLAN = "tempVlan";
    private static final String DEFAULTPRIORITY = "defaultPriority";

    /**
     * Returns bridgeId.
     *
     * @return bridgeId, or null
     */
    public String bridgeId() {
        JsonNode jsonNode = object.get(BRIDGEID);
        if (jsonNode == null) {
            return null;
        }

        try {
            return jsonNode.asText();
        } catch (IllegalArgumentException e) {
            log.error("Wrong bridgeID format {}", jsonNode.asText());
            return null;
        }
    }

    /**
     * Returns dataPlanePort.
     *
     * @return dataPlanePort, or null
     */
    public long dataPlanePort() {
        JsonNode jsonNode = object.get(DATAPLANEPORT);
        if (jsonNode == null) {
            return 0;
        }

        try {
            return jsonNode.asLong();
        } catch (IllegalArgumentException e) {
            log.error("Wrong port format {}", jsonNode.asLong());
            return 0;
        }
    }

    /**
     * Returns vmPort1.
     *
     * @return vmPort1, or null
     */
    public long vmPort1() {
        JsonNode jsonNode = object.get(VMPORT1);
        if (jsonNode == null) {
            return 0;
        }

        try {
            return jsonNode.asLong();
        } catch (IllegalArgumentException e) {
            log.error("Wrong port format {}", jsonNode.asLong());
            return 0;
        }
    }

    /**
     * Returns vmPort2.
     *
     * @return vmPort2, or null
     */
    public long vmPort2() {
        JsonNode jsonNode = object.get(VMPORT2);
        if (jsonNode == null) {
            return 0;
        }

        try {
            return jsonNode.asLong();
        } catch (IllegalArgumentException e) {
            log.error("Wrong port format {}", jsonNode.asLong());
            return 0;
        }
    }

    /**
     * Returns siteVlan.
     *
     * @return siteVlan, or null
     */
    public short siteVlan() {
        JsonNode jsonNode = object.get(SITEVLAN);
        if (jsonNode == null) {
            return 0;
        }

        try {
            return (short) jsonNode.asInt();
        } catch (IllegalArgumentException e) {
            log.error("Wrong vlan format {}", jsonNode.asInt());
            return 0;
        }
    }

    /**
     * Returns tempVlan.
     *
     * @return tempVlan, or null
     */
    public short tempVlan() {
        JsonNode jsonNode = object.get(TEMPVLAN);
        if (jsonNode == null) {
            return 0;
        }

        try {
            return (short) jsonNode.asInt();
        } catch (IllegalArgumentException e) {
            log.error("Wrong vlan format {}", jsonNode.asInt());
            return 0;
        }
    }

    /**
     * Returns tempVlan.
     *
     * @return tempVlan, or null
     */
    public int defaultPriority() {
        JsonNode jsonNode = object.get(DEFAULTPRIORITY);
        if (jsonNode == null) {
            return 0;
        }

        try {
            return jsonNode.asInt();
        } catch (IllegalArgumentException e) {
            log.error("Wrong priority format {}", jsonNode.asInt());
            return 0;
        }
    }

}

