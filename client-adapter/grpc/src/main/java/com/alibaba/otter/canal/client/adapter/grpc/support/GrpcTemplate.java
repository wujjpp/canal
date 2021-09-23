
/**
 * Created by Wu Jian Ping on - 2021/09/22.
 */
package com.alibaba.otter.canal.client.adapter.grpc.support;

import java.util.Map;
import java.util.Random;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.otter.canal.client.adapter.grpc.CountResponse;
import com.alibaba.otter.canal.client.adapter.grpc.NotifyRequest;
import com.alibaba.otter.canal.client.adapter.grpc.NotifyServiceGrpc;
import com.google.protobuf.Empty;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class GrpcTemplate {
    private static Logger logger = LoggerFactory.getLogger(GrpcTemplate.class);

    private Random seed = new Random();

    private String hosts;
    private String sign;
    private int poolSize = 1;

    private List<NotifyServiceClient> clients = new ArrayList<>();

    public GrpcTemplate(String hosts, int poolSize, String sign) {
        this.hosts = hosts;
        this.poolSize = poolSize;
        this.sign = sign;

        String[] arr = this.hosts.split(",");

        for (String host : arr) {
            if (host.trim() != "") {
                for (int i = 0; i < this.poolSize; ++i) {
                    this.clients.add(new NotifyServiceClient(host.trim()));
                }
            }
        }

        if (this.clients.size() == 0) {
            throw new RuntimeException("Grpc clients is empty, please confirm `hosts` and `poolSize` property");
        }
    }

    public void insert(String database, String table, Map<String, Object> data) {
        this.runAsync(database, table, "insert", data);
    }

    public void update(String database, String table, Map<String, Object> data) {
        this.runAsync(database, table, "update", data);

    }

    public void delete(String database, String table, Map<String, Object> data) {
        this.runAsync(database, table, "delete", data);
    }

    public CompletableFuture<Boolean> runAsync(String database, String table, String action, Map<String, Object> data) {
        try {
            if (logger.isDebugEnabled()) {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("database", database);
                body.put("table", table);
                body.put("action", action);
                body.put("data", data);
                body.put("sign", this.sign);

                logger.debug("{}", JSON.toJSONString(body, SerializerFeature.WriteMapNullValue));
            }

            this.getClient().notify(database, table, action, data, sign);

            return completedFuture(true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return completedFuture(false);
        }
    }

    public int count() {
        return this.getClient().count();
    }

    private NotifyServiceClient getClient() {
        int index = this.seed.nextInt(this.clients.size());
        return this.clients.get(index);
    }

    public static class NotifyServiceClient {
        private final NotifyServiceGrpc.NotifyServiceBlockingStub blockingStub;
        private final ManagedChannel channel;

        public NotifyServiceClient(String host) {
            this.channel = ManagedChannelBuilder.forTarget(host).usePlaintext().build();
            this.blockingStub = NotifyServiceGrpc.newBlockingStub(this.channel);
        }

        public void notify(String database, String table, String action, Map<String, Object> data, String sign) {
            NotifyRequest request = NotifyRequest.newBuilder().setDatabase(database).setTable(table).setAction(action)
                    .setData(JSON.toJSONString(data, SerializerFeature.WriteMapNullValue)).setSign(sign).build();

            blockingStub.notify(request);
        }

        public int count() {
            Empty request = Empty.newBuilder().build();
            CountResponse response = blockingStub.count(request);
            return response.getCount();
        }

        public void destroy() {
            if (this.channel != null) {
                try {
                    this.channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
                } catch (Exception e) {
                }
            }
        }
    }
}
