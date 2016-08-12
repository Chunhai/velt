# velt

To populate some flow rules for vnf purpose

example cfg file:

{
"apps" : {
        "org.onosproject.velt" : {
           "veltcfg" : {
             "bridgeId" : "of:0000000000000001",
             "dataPlanePort" : 26,
             "vmPort1" : 30,
             "vmPort2" : 31,
             "siteVlan" : 100,
             "tempVlan" : 200,
             "defaultPriority" : 60000
             }
         }
      }
}
