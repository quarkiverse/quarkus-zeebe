# Examples

Start `zeebe` and `jaeger` environment
```shell
docker-compose up
```

Start example application
```shell
mvn clean compile quarkus:dev
```

## Opentracing test1

Start `test1` process instance.
```shell
curl -X POST -H "Content-Type: application/json" -d '{"data":"1"}' http://localhost:8080/process/start/test1
```
