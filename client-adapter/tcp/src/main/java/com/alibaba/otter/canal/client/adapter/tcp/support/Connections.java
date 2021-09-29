
/**
 * Created by Wu Jian Ping on - 2021/09/28.
 */
package com.alibaba.otter.canal.client.adapter.tcp.support;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.Socket;

public class Connections {
    private static Logger logger = LoggerFactory.getLogger(Connections.class);
    private Random seed = new Random();

    private List<TcpClient> clients = new ArrayList<>();

    public Connections(String hosts, int poolSize) {
        // 解出主机列表
        String[] arr = hosts.split(",");

        for (String host : arr) {
            if (host.trim() != "") {
                // 分离出ip和port
                String[] list = host.split(":");
                String ip = list[0];
                int port = Integer.parseInt(list[1]);
                // 根据连接池大小创建连接
                for (int i = 0; i < poolSize; ++i) {
                    this.clients.add(new TcpClient(ip, port));
                }
            }
        }

        if (this.clients.size() == 0) {
            throw new RuntimeException("Tcp clients is empty, please confirm `hosts` and `poolSize` property");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Client count: {}", this.clients.size());
        }
    }

    public TcpClient getClient() {
        List<TcpClient> connectedClients = new ArrayList<>();

        for (TcpClient c : this.clients) {
            if (c.getIsConnected()) {
                connectedClients.add(c);
            }
        }

        if (connectedClients.size() > 0) {
            int index = this.seed.nextInt(connectedClients.size());
            return connectedClients.get(index);
        }
        return null;
    }

    public Boolean send(String data) {
        TcpClient c = this.getClient();
        if (c != null) {
            return c.send(data);
        }
        return false;
    }

    public static class TcpClient {
        private String host;
        private int port;

        private Socket socket;
        private OutputStream stream;
        private Boolean isConnected = false;
        private Boolean isConnecting = false;

        public TcpClient(String host, int port) {
            this.host = host;
            this.port = port;

            this.connect();
        }

        private void connect() {
            if (this.isConnecting) {
                return;
            }

            synchronized (this) {
                if (this.isConnecting) {
                    return;
                }
                while (true) {
                    this.isConnecting = true;
                    try {
                        this.socket = new Socket(this.host, this.port);
                        this.stream = socket.getOutputStream();
                        this.isConnected = true;
                        this.isConnecting = false;
                        logger.info("conntected");
                        break;
                    } catch (Exception e) {
                        this.isConnected = false;
                        logger.error(e.getMessage(), e);
                    }

                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        public Boolean getIsConnected() {
            return this.isConnected;
        }

        public Boolean send(String data) {
            if (this.isConnected) {
                try {
                    byte[] d = data.getBytes();
                    byte[] length = new byte[4];
                    length[0] = (byte) (d.length / (256 * 256 * 256));
                    length[1] = (byte) ((d.length % (256 * 256 * 256)) / (256 * 256));
                    length[2] = (byte) ((d.length % (256 * 256)) / (256));
                    length[3] = (byte) (d.length % 256);
                    this.stream.write(length);
                    this.stream.write(d);
                    return true;
                } catch (Exception e) {
                    this.isConnected = false;
                    this.connect(); // 尝试重连
                    logger.error(e.getMessage(), e);
                }
            }
            return false;
        }
    }
}
