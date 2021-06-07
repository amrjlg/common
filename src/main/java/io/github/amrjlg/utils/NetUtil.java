/*
 *  Copyright (c) 2021-2021 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.amrjlg.utils;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Net utils.
 * <p>
 * copy from nacos @see com.alibaba.nacos.api.utils.NetUtils
 * </p>
 *
 * @author xuanyin.zy
 */
public class NetUtil {

    private static String localIp;

    /**
     * Get local ip.
     *
     * @return local ip
     */
    public static String localIP() {
        if (!StringUtil.isEmpty(localIp)) {
            return localIp;
        }

        String ip = findFirstNonLoopbackAddress();

        return localIp = ip;

    }

    private static String findFirstNonLoopbackAddress() {
        InetAddress result = null;

        try {
            find:
            for (Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
                 nics.hasMoreElements(); ) {
                NetworkInterface ifc = nics.nextElement();
                if (ifc.isUp()) {
                    for (Enumeration<InetAddress> addrs = ifc.getInetAddresses(); addrs.hasMoreElements(); ) {
                        InetAddress address = addrs.nextElement();
                        boolean isLegalIpVersion =
                                Boolean.parseBoolean(System.getProperty("java.net.preferIPv6Addresses"))
                                        ? address instanceof Inet6Address : address instanceof Inet4Address;
                        if (isLegalIpVersion && !address.isLoopbackAddress()) {
                            result = address;
                            break find;
                        }
                    }

                }
            }
        } catch (SocketException ex) {
            //ignore
        }

        try {
            result = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            //ignore
        }
        if (result != null) {
            return result.getHostAddress();
        }


        throw new IllegalStateException("resolve_failed");

    }

    public static List<String> getLocalIPList() {
        Predicate<NetworkInterface> predicate = n -> {
            try {
                return n.isUp() && !n.isVirtual();
            } catch (SocketException e) {
                return false;
            }
        };
        Function<InetAddress, String> function = i->{
            if (i.isLoopbackAddress()){
                return i.getHostAddress();
            }else {
                return null;
            }
        };
        return parseNetworkInterface(predicate, function);
    }

    public static <O> List<O> parseNetworkInterface(Predicate<NetworkInterface> predicate, Function<? super InetAddress, O> function) {
        List<O> results = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (predicate.test(networkInterface)) {
                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        O res = function.apply(inetAddress);
                        if (res != null) {
                            results.add(res);
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return results;
    }
}
