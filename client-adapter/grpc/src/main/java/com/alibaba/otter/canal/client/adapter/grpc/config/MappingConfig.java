/**
 * Created by Wu Jian Ping on - 2021/09/22.
 */

package com.alibaba.otter.canal.client.adapter.grpc.config;

import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.client.adapter.support.AdapterConfig;

public class MappingConfig implements AdapterConfig {

    private String dataSourceKey; // 数据源key

    private String destination; // canal实例或MQ的topic

    private String groupId; // groupId

    private String outerAdapterKey; // 对应适配器的key

    private GrpcMapping grpcMapping; // grpc映射配置

    @Override
    public String getDataSourceKey() {
        return dataSourceKey;
    }

    public void setDataSourceKey(String dataSourceKey) {
        this.dataSourceKey = dataSourceKey;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getOuterAdapterKey() {
        return outerAdapterKey;
    }

    public void setOuterAdapterKey(String outerAdapterKey) {
        this.outerAdapterKey = outerAdapterKey;
    }

    public void validate() {
    }

    public GrpcMapping getGrpcMapping() {
        return grpcMapping;
    }

    public void setGrpcMapping(GrpcMapping grpcMapping) {
        this.grpcMapping = grpcMapping;
    }

    @Override
    public GrpcMapping getMapping() {
        return this.grpcMapping;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public static class GrpcMapping implements AdapterMapping {
        private String etlCondition; // etl条件sql
        private String serviceUrl; // http接口地址
        private List<MonitorTable> monitorTables = new ArrayList<>();
        private EtlSetting etlSetting;

        public String getEtlCondition() {
            return etlCondition;
        }

        public void setEtlCondition(String etlCondition) {
            this.etlCondition = etlCondition;
        }

        public String getServiceUrl() {
            return this.serviceUrl;
        }

        public void setServiceUrl(String url) {
            this.serviceUrl = url;
        }

        public List<MonitorTable> getMonitorTables() {
            return this.monitorTables;
        }

        public void setMonitorTables(List<MonitorTable> monitorTables) {
            this.monitorTables = monitorTables;
        }

        public EtlSetting getEtlSetting() {
            return this.etlSetting;
        }

        public void setEtlSetting(EtlSetting etlSetting) {
            this.etlSetting = etlSetting;
        }
    }

    public static class EtlSetting {
        private String database;
        private String table;
        private String condition;

        public String getDatabase() {
            return this.database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public String getTable() {
            return this.table;
        }

        public void setTable(String table) {
            this.table = table;
        }

        public String getCondition() {
            return this.condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }
    }

    public static class MonitorTable {
        private String tableName;
        private List<String> actions = new ArrayList<>();

        public String getTableName() {
            return this.tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public List<String> getActions() {
            return this.actions;
        }

        public void setActions(List<String> actions) {
            this.actions = actions;
        }
    }
}
