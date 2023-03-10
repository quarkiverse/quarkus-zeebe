

By default all job-worker are disabled.

Start application
```shell
quarkus dev
```
Reset counter
```shell
curl http://localhost:8080/test/reset
```
Create a process instances
```shell
curl http://localhost:8080/test/blocking-process
```
Available process are: 
* blocking-process
* blocking-auto-process
* uni-auto-process
* completionStage-auto-process

Enable job-worker for the process in the `application.properties`.
Restart the application with `s`
