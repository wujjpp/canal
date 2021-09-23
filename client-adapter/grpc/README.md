# grpc adapter

## `application.yml`

```yml
  canalAdapters:
  - instance: logstore # canal instance Name or mq topic name
    groups:
    - groupId: g1
      outerAdapters:
      - name: grpc
        key: user_grpc
        properties:
          # 逻辑说明："hosts"用于指定多个grpc server, poolSize指定为每个host创建多少个Client，主要用于负载均衡
          hosts: 127.0.0.1:9001,127.0.0.1:9002
          poolSize: 2
          sign: 123
```

## adapter config

```yml
dataSourceKey: defaultDS
destination: logstore
groupId: g1
outerAdapterKey: user_grpc

grpcMapping:
  etlSetting:
    database: logstore
    table: user
    condition: "where userId >= {}"
  
  monitorTables:
    - tableName: logstore.user

    - tableName: logstore.role
      actions:
        - INSERT

    - tableName: logstore.account
      actions:
        - DELETE

```
