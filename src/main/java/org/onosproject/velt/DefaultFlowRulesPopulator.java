/*
 * Copyright 2016-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.velt;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.packet.VlanId;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.config.ConfigFactory;
import org.onosproject.net.config.NetworkConfigEvent;
import org.onosproject.net.config.NetworkConfigListener;
import org.onosproject.net.config.NetworkConfigRegistry;
import org.onosproject.net.config.basics.SubjectFactories;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.onlab.util.Tools.groupedThreads;

/**
 * Populate some flow rules for vnf purpose.
 * Created by Chunhai on 8/11.
 * All rights reserved.
 */
@Component(immediate = true)
public class DefaultFlowRulesPopulator {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowObjectiveService flowObjectiveService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetworkConfigRegistry configRegistry;

    //@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    //protected NetworkConfigService configService;

    private final NetworkConfigListener configListener = new InternalConfigListener();

    private final ConfigFactory configFactory =
            new ConfigFactory(SubjectFactories.APP_SUBJECT_FACTORY, VeltConfig.class, "veltcfg") {
                @Override
                public VeltConfig createConfig() {
                    return new VeltConfig();
                }
            };

    private final ExecutorService eventExecutor =
            newSingleThreadExecutor(groupedThreads("onos/velt", "event-handler", log));

    private static final String SR_APP_ID = "org.onosproject.velt";
    private String bridgeId;
    private int defaultProiority;
    private long dataInterface;
    private long vmInterface1;
    private long vmInterface2;
    private short siteVlan;
    private short tempVlan;

    protected DeviceId bridgeID;
    protected ApplicationId appId;


    @Activate
    protected void activate() {

        log.info("Started");

        appId = coreService.registerApplication(SR_APP_ID);
        //configService.addListener(configListener);
        configRegistry.registerConfigFactory(configFactory);
        configRegistry.addListener(configListener);

    }

    @Deactivate
    protected void deactivate() {

        flowRuleService.removeFlowRulesById(appId);
        //configService.removeListener(configListener);
        configRegistry.unregisterConfigFactory(configFactory);
        configRegistry.removeListener(configListener);
        eventExecutor.shutdown();

        log.info("Stopped");
    }

    private void populateFlowRules() {

        bridgeID = DeviceId.deviceId(bridgeId);

        for (Device device : deviceService.getDevices()) {
            if (device.id().equals(bridgeID)) {

                log.info("Populate flow rule for: {} ", bridgeId);

                installDefaultRule(PortNumber.portNumber(dataInterface), PortNumber.portNumber(vmInterface1),
                                   siteVlan, (short) 0);
                installDefaultRule(PortNumber.portNumber(vmInterface2), PortNumber.portNumber(dataInterface),
                                   siteVlan, tempVlan);
                installDefaultRule(PortNumber.portNumber(dataInterface), PortNumber.portNumber(vmInterface2),
                                   tempVlan, (short) 0);
                installDefaultRule(PortNumber.portNumber(vmInterface1), PortNumber.portNumber(dataInterface),
                                   tempVlan, siteVlan);
            }
        }
    }

    private void installDefaultRule(PortNumber inPortNumber, PortNumber outPortNumber, short inVlan, short outVlan) {

        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder()
                .matchInPort(inPortNumber)
                .matchVlanId(VlanId.vlanId(inVlan));


        TrafficTreatment.Builder builder = DefaultTrafficTreatment.builder();

        if (outVlan != (short) 0) {
            builder.setVlanId(VlanId.vlanId(outVlan));
        }

        TrafficTreatment treatment = builder.setOutput(outPortNumber)
                .build();

        ForwardingObjective forwardingObjective = DefaultForwardingObjective.builder()
                .withSelector(selectorBuilder.build())
                .withTreatment(treatment)
                .withPriority(defaultProiority)
                .withFlag(ForwardingObjective.Flag.VERSATILE)
                .fromApp(appId)
                .makePermanent()
                .add();

        flowObjectiveService.forward(DeviceId.deviceId(bridgeId),
                                     forwardingObjective);
    }

    /**
     * Reads cfg information from config file.
     */
    private void readConfiguration() {

        VeltConfig config = configRegistry.getConfig(appId, VeltConfig.class);
        if (config == null) {
            log.debug("No configuration found");
            return;
        }

        log.info("Load Velt configurations");

        bridgeId = config.bridgeId();
        defaultProiority = config.defaultPriority();
        dataInterface = config.dataPlanePort();
        vmInterface1 = config.vmPort1();
        vmInterface2 = config.vmPort2();
        siteVlan = config.siteVlan();
        tempVlan = config.tempVlan();

        populateFlowRules();

        log.info("Complete populating flow rules");

    }

    private class InternalConfigListener implements NetworkConfigListener {

        @Override
        public void event(NetworkConfigEvent event) {
            if (!event.configClass().equals(VeltConfig.class)) {
                log.info("App domain in the configure file is wrong");
                return;
            }

            switch (event.type()) {
                case CONFIG_ADDED:
                case CONFIG_UPDATED:
                    readConfiguration();
                    break;
                default:
                    break;
            }
        }
    }

}
