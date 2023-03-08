
### Zeebe dev monitor

Create new process instance in the `zeebe-dev-montor` with these variables
```json
{"_name":"Test","_birth":"2001-08-16"}
```

`NoName` person
```json
{"_name":"NoName","_birth":"2001-08-16"}
```

### Curl

Start process with curl for `Test` person
```shell
curl http://localhost:8080/person -X POST -H 'Content-Type: application/json' -d '{"name":"Test-Curl","birth":"2001-08-16"}'
```

`NoName` person
```shell
curl http://localhost:8080/person -X POST -H 'Content-Type: application/json' -d '{"name":"Test-Curl","birth":"2001-08-16"}'
```