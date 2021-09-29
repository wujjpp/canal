# tcp adapter

## `application.yml`

```yml
  canalAdapters:
  - instance: logstore # canal instance Name or mq topic name
    groups:
    - groupId: g1
      outerAdapters:
      - name: tcp
        key: user_tcp
        properties:
          hosts: 127.0.0.1:10002,127.0.0.1:10003
          poolSize: 1
          serviceUrl: http://127.0.0.1:9001/sync
          sign: 123
```

## adapter config

```yml
dataSourceKey: defaultDS
destination: logstore
groupId: g1
outerAdapterKey: user_tcp

tcpMapping:
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
