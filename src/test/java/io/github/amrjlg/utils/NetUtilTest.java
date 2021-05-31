package io.github.amrjlg.utils;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

class NetUtilTest {

    @Test
    void localIP() throws UnknownHostException {
        System.out.println(NetUtil.localIP());
        System.out.println(InetAddress.getLocalHost().getHostAddress());


        System.out.println(NetUtil.getLocalIPList());

    }
}