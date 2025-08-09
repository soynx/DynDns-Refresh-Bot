package com.dns.refresh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DynDNSUpdater {
    public static Logger logger = LoggerFactory.getLogger(DynDNSUpdater.class);
    public static int httpTimeoutMs = Integer.parseInt(System.getProperty("timeouts.http", "5000"));

    /**
     * Sends a GET request to the given DynDNS update URL and returns whether it was successful.
     *
     * @param updateUrl the full DynDNS update URL including token/domain (e.g. from duckdns or ipv64)
     * @return true if HTTP response is 200 OK, false otherwise
     */
    public static boolean updateDynDNS(String updateUrl) {
        logger.debug("Updating DynDNS with url {}", updateUrl);
        HttpURLConnection connection = null;
        try {
            URL url = new URL(updateUrl);
            connection = (HttpURLConnection) url.openConnection();

            // Force HTTPS if not already handled outside
            if (!"https".equalsIgnoreCase(url.getProtocol())) {
                throw new IllegalArgumentException("Only HTTPS URLs are allowed for security reasons.");
            }

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(httpTimeoutMs); // 5s timeout
            connection.setReadTimeout(httpTimeoutMs);
            connection.setInstanceFollowRedirects(false); // prevent auto-following for security

            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK; // 200

        } catch (IOException e) {
            logger.error("DynDNS update failed: {}", e.getMessage());
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static String getPublicIP() {
        logger.trace("Getting public IP");

        String[] urls = {
                "https://api.ipify.org?format=text",
                "https://checkip.amazonaws.com/",
                "https://ifconfig.me/ip",
                "https://icanhazip.com/",
                "https://ident.me/"
        };

        for (String urlStr : urls) {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(httpTimeoutMs);
                connection.setReadTimeout(httpTimeoutMs);
                connection.setRequestMethod("GET");

                int status = connection.getResponseCode();
                if (status == 200) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()))) {
                        String ip = reader.readLine();
                        if (ip != null && !ip.isEmpty()) {
                            logger.info("Successfully retrieved public IP from {}", urlStr);
                            return ip.trim();
                        }
                    }
                } else {
                    logger.warn("Failed to get Public-IP from {}: HTTP {}", urlStr, status);
                }
            } catch (Exception e) {
                logger.warn("Error getting Public-IP from {}: {}", urlStr, e.toString());
            }
        }

        throw new RuntimeException("Unable to determine public IP from any source");
    }

}
