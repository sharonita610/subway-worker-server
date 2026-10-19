package com.game.subwayworkerserver.toss.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

public final class NetworkUtil {

    private static final String DEFAULT_HOST = "localhost";

    private NetworkUtil() {
    }

    /**
     * 시스템의 외부 통신이 가능한 로컬 IPv4 주소를 찾습니다.
     * * @return 유효한 로컬 IP 주소 (예: "192.168.1.10") 또는 찾지 못할 경우 "localhost"
     */
    public static String getLocalIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces == null) {
                return DEFAULT_HOST;
            }

            for (NetworkInterface networkInterface : Collections.list(interfaces)) {

                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                for (InetAddress addr : Collections.list(addresses)) {

                    if (addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }

        } catch (SocketException e) {
            System.err.println("Failed to retrieve network interfaces: " + e.getMessage());
        }

        return DEFAULT_HOST;
    }
}
