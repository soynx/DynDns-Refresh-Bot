package com.dns.refresh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {

    public static Logger logger = LoggerFactory.getLogger(Main.class);
    public static int loopTimeout = Integer.parseInt(System.getProperty("timeouts.loop", "10"));
    private String currentIpAddress = System.getProperty("startup.ip", "<ip_address_not_set_yet>");
    private String[] refreshDomains = System.getProperty("refresh.domains", "").split(";");

    public static void main(String[] args) {
        logger.info("Starting Refresh Bot");
        new Main().mainloop();
    }

    public void mainloop() {
        if (refreshDomains.length == 0) {
            logger.error("No refresh domains specified");
            throw new RuntimeException("No refresh domains specified");
        }

        while (true) {

            try {
                Thread.sleep(loopTimeout);
            } catch (InterruptedException e) {
                logger.warn("Could not perform a thread sleep!");
            }

            String newIpAddress;

            try {
                 newIpAddress = DynDNSUpdater.getPublicIP();

                 if (newIpAddress == null) {
                     throw new RuntimeException("Could not get public IP address");
                 }
            } catch (Exception e) {
                logger.error("Could not get public IP address!: {}", e.getMessage());
                continue;
            }

            if (currentIpAddress.equals(newIpAddress)) {
                logger.debug("No changes inside the ip-address registered. old: {}, new: {}", currentIpAddress, newIpAddress);
            } else {
                logger.info("New IP address found: {}!", newIpAddress);
                currentIpAddress = newIpAddress;

                for (String refreshDomain : refreshDomains) {
                    DynDNSUpdater.updateDynDNS(refreshDomain);
                    logger.info("New IP address {} updated with url: {}!", newIpAddress, refreshDomain);
                }
            }
        }
    }
}