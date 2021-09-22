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
          serviceUrl: http://127.0.0.1/sync
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
