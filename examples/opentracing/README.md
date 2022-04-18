# Examples

Start `zeebe` and `jaeger` environment
```shell
docker-compose up
```

Start example application
```shell
mvn clean compile quarkus:dev
```

## Opentracing test

Start `test` process instances.

```shell
curl http://localhost:8080/process/test
```
