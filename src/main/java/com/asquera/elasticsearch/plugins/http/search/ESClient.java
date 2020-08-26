package com.asquera.elasticsearch.plugins.http.search;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;

public class ESClient {


    private static TransportClient transPort = null;
    private static String esServerIp;
    private static String port;
    private static String clusterName;
    private static String userName;
    private static String password;
    private static String httpPort;

    public synchronized static void init() {
        try {
            if (transPort == null) {

                Settings settings = Settings.builder()
                        .put("cluster.name", clusterName)
                        //.put("xpack.security.user", userName + ":" + password)
                        .put("client.transport.sniff", true)
                        .build();
                // 创建client
                //transPort = new PreBuiltXPackTransportClient(settings).addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(esServerIp), Integer.valueOf(port)));

                transPort = new PreBuiltTransportClient(settings);
                String[] ips = esServerIp.split(",");
                for (String ip : ips) {
                    transPort.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ip), Integer.valueOf(port)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static TransportClient getInstance(){
        if (transPort == null) {
            init();
        }
        return transPort;
    }

    public static String getEsServerIp() {
        return esServerIp;
    }

    public static void setEsServerIp(String esServerIp) {
        ESClient.esServerIp = esServerIp;
    }

    public static String getPort() {
        return port;
    }

    public static void setPort(String port) {
        ESClient.port = port;
    }

    public static String getClusterName() {
        return clusterName;
    }

    public static void setClusterName(String clusterName) {
        ESClient.clusterName = clusterName;
    }

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String userName) {
        ESClient.userName = userName;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        ESClient.password = password;
    }

    public static String getHttpPort() {
        return httpPort;
    }

    public static void setHttpPort(String httpPort) {
        ESClient.httpPort = httpPort;
    }
}
