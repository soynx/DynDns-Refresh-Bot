package com.dns.refresh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class Main {

    public static Logger logger = LoggerFactory.getLogger(Main.class);
    public static int loopTimeout = Integer.parseInt(System.getProperty("timeouts.loop", "10"));
    private final String[] refreshDomains = System.getProperty("refresh.domains", "").split(";");
    private String currentIpAddress = System.getProperty("startup.ip", "<ip_address_not_set_yet>");

    public static void main(String[] args) {
        logger.info("Starting Refresh Bot");
        if (System.getenv("JAVA_OPTS") == null || System.getenv("JAVA_OPTS").isEmpty()) {
            logger.warn("No Properties defined over env-variables!");
            logger.warn("We recommend to use the env-variable 'JAVA_OPTS' to set properties inside JVM");
            logger.warn("Example: '$JAVA_OPTS=\"-Drefresh.domains=https://my.dyndns.org\"'");
        }
        new Main().mainloop();
    }

    public static String censoreUrl(String url) {
        if (url == null || url.isBlank()) return null;

        try {
            // Ensure URI parsing works even if scheme is missing
            String normalizedUrl = url.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*") ? url : "http://" + url;

            URI uri = new URI(normalizedUrl);
            String scheme = uri.getScheme();
            String host = uri.getHost();

            if (scheme == null || host == null) {
                return null;
            }

            // Remove "www." if present
            String domain = host.startsWith("www.") ? host.substring(4) : host;

            return scheme + "://" + domain;

        } catch (URISyntaxException e) {
            return null;
        }
    }

    public void mainloop() {
        if (refreshDomains.length == 0 || System.getProperty("refresh.domains", "") == null) {
            logger.error("No refresh domains specified");
            throw new RuntimeException("No refresh domains specified");
        } else {
            for (String domain : refreshDomains) {
                logger.info("Got refresh-domain: {}", censoreUrl(domain));
            }
        }

        while (true) {
            try {
                Thread.sleep(loopTimeout * 1000);
            } catch (InterruptedException e) {
                logger.warn("Could not perform a thread sleep!");
            }

            String newIpAddress;

            try {
                newIpAddress = DynDNSUpdater.getPublicIP();
                logger.debug("Got current ip from api: {}", newIpAddress);

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
                    logger.info("New IP address {} updated with url: {}!", newIpAddress, censoreUrl(refreshDomain));
                }
            }
        }
    }
}