# Examples

Start base example application
```shell
mvn clean compile quarkus:dev
```
Start `tests` process instances.

```shell
curl http://localhost:8080/process/test
```

Start 'test.complete' process instance.

```shell
curl http://localhost:8080/process/test/test.complete
```
