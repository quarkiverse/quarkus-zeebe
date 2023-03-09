

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

Result
```shell
blocking:               Current throughput (jobs/s ): 9, Max: 17
blocking-auto:          Current throughput (jobs/s ): 10, Max: 19
uni-auto:               Current throughput (jobs/s ): 250, Max: 380
completionStage-auto:   Current throughput (jobs/s ): 250, Max: 380
```