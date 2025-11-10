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
        // 유틸리티 클래스는 인스턴스화 방지
    }

    /**
     * 시스템의 외부 통신이 가능한 로컬 IPv4 주소를 찾습니다.
     * Node.js의 os.networkInterfaces() 로직을 대체합니다.
     * * @return 유효한 로컬 IP 주소 (예: "192.168.1.10") 또는 찾지 못할 경우 "localhost"
     */
    public static String getLocalIp() {
        try {
            // 시스템의 모든 네트워크 인터페이스를 가져옵니다.
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces == null) {
                return DEFAULT_HOST;
            }

            // 각 인터페이스를 순회합니다.
            for (NetworkInterface networkInterface : Collections.list(interfaces)) {

                // 루프백(lo) 인터페이스는 건너뜁니다 (Node.js의 !iface.internal 역할)
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                // 해당 인터페이스의 모든 주소를 순회합니다.
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                for (InetAddress addr : Collections.list(addresses)) {

                    // IPv4 주소인지 확인합니다. (Node.js의 iface.family === 'IPv4' 역할)
                    if (addr instanceof Inet4Address) {
                        return addr.getHostAddress(); // 찾은 첫 번째 IPv4 주소를 반환
                    }
                }
            }

        } catch (SocketException e) {
            // 네트워크 인터페이스 조회 실패 시 오류 처리
            System.err.println("Failed to retrieve network interfaces: " + e.getMessage());
        }

        // 유효한 IP를 찾지 못하면 기본값 반환
        return DEFAULT_HOST;
    }
}
