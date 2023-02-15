# Quarkus Zeebe


[![Build](https://github.com/quarkiverse/quarkus-zeebe/workflows/Build/badge.svg?branch=main)](https://github.com/quarkiverse/quarkus-zeebe/actions?query=workflow%3ABuild)
[![License](https://img.shields.io/github/license/quarkiverse/quarkus-zeebe.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Central](https://img.shields.io/maven-central/v/io.quarkiverse.zeebe/quarkus-zeebe-parent?color=green)](https://search.maven.org/search?q=g:io.quarkiverse.zeebe%20AND%20a:quarkus-zeebe-parent)
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-1-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

## Usage

To use the extension, add the dependency to the target project:
```xml
<dependency>
    <groupId>io.quarkiverse.zeebe</groupId>
    <artifactId>quarkus-zeebe</artifactId>
    <version>{version}</version>
</dependency>
```
### Upgrade

>In version `>=0.8.0` we replace `@ZeebeWorker` with `@JobWorker` annotation.

>In version `>0.7.0` we removed the hazelcast dependency and [zeebe-simple-monitor](https://github.com/camunda-community-hub/zeebe-simple-monitor)
for test and dev services. Now we do use [zeebe-test-container](https://github.com/camunda-community-hub/zeebe-test-container) with [debug exporter](https://github.com/camunda-community-hub/zeebe-test-container#debug-exporter) 
and [zeebe-dev-monitor](https://github.com/lorislab/zeebe-dev-monitor). In test module we remove our assert API and switch to Camunda [BpmnAssert](https://github.com/camunda/zeebe-process-test/blob/main/assertions/src/main/java/io/camunda/zeebe/process/test/assertions/BpmnAssert.java) 
from [zeebe-process-test](https://github.com/camunda/zeebe-process-test).

API migration

| 0.6.x                                | 0.7.x                                               |
|--------------------------------------|-----------------------------------------------------|
| io.quarkiverse.zeebe.test.BpmnAssert | io.camunda.zeebe.process.test.assertions.BpmnAssert |


## Configuration

Build configuration
```properties
# enable auto load bpmn resources true|false
quarkus.zeebe.resources.enabled=true
# src/main/resources/bpmn 
quarkus.zeebe.resources.location=bpmn
# enable health check true|false
quarkus.zeebe.health.enabled=false
# enable opentracing true|false
quarkus.zeebe.tracing.enabled=true
# enable metrics true|false
quarkus.zeebe.metrics.enabled=true
```

Runtime configuration
```properties
# broker configuration
quarkus.zeebe.client.broker.gateway-address=localhost:26500
quarkus.zeebe.client.broker.keep-alive=PT45S
# cloud configuration
quarkus.zeebe.client.cloud.cluster-id=
quarkus.zeebe.client.cloud.client-id=
quarkus.zeebe.client.cloud.client-secret=
quarkus.zeebe.client.cloud.region=bru-2
quarkus.zeebe.client.cloud.base-url=zeebe.camunda.io
quarkus.zeebe.client.cloud.auth-url=https://login.cloud.camunda.io/oauth/token
quarkus.zeebe.client.cloud.port=443
quarkus.zeebe.client.cloud.credentials-cache-path=
# message configuration
quarkus.zeebe.client.message.time-to-live=PT1H
# security configuration
quarkus.zeebe.client.security.plaintext=true
quarkus.zeebe.client.security.cert-path=
# job configuration
quarkus.zeebe.client.job.max-jobs-active=32
quarkus.zeebe.client.job.worker-execution-threads=1
quarkus.zeebe.client.job.worker-name=default
quarkus.zeebe.client.job.default-type=
quarkus.zeebe.client.job.timeout=PT5M
quarkus.zeebe.client.job.request-timeout=
quarkus.zeebe.client.job.pool-interval=PT0.100S
quarkus.zeebe.client.job.exp-backoff-factor=1.6
quarkus.zeebe.client.job.exp-jitter-factor=0.1
quarkus.zeebe.client.job.exp-max-delay=5000
quarkus.zeebe.client.job.exp-min-delay=50
# overwrite job handler annotation
quarkus.zeebe.client.workers.<type>.name=
quarkus.zeebe.client.workers.<type>.enabled=
quarkus.zeebe.client.workers.<type>.timeout=
quarkus.zeebe.client.workers.<type>.max-jobs-active=
quarkus.zeebe.client.workers.<type>.request-timeout=
quarkus.zeebe.client.workers.<type>.poll-interval=
# Auto-complete
quarkus.zeebe.client.auto-complete.max-retries=20
quarkus.zeebe.client.auto-complete.retry-delay=50L
quarkus.zeebe.client.auto-complete.exp-backoff-factor=1.5
quarkus.zeebe.client.auto-complete.exp-jitter-factor=0.2
quarkus.zeebe.client.auto-complete.exp-max-delay=1000
quarkus.zeebe.client.auto-complete.exp-min-delay=50

# client tracing configuration
quarkus.zeebe.client.tracing.attributes=
# bpmn-process-id,bpmn-process-instance-key,bpmn-process-element-id,
# bpmn-process-element-instance-key,bpmn-process-def-key,bpmn-process-def-ver,bpmn-retries,bpmn-component,
# bpmn-job-type,bpmn-job-key,bpmn-class,bpmn-class-method
```

### Exemplary Setup for your local development
Generally speaking there are three ways to configure your quarkus project to speak with camunda:
- Local dev instance with dev services
- Shared local dev instance
- Direct interaction with Camunda SaaS/ on-premise

You can see some exemplary configurations for each of the setups below. Please note that these are only exemplary and can be adapted to your needs.

#### Local dev instance with dev services

```properties
# enable auto load bpmn resources 
quarkus.zeebe.resources.enabled=true
# src/main/resources/bpmn
quarkus.zeebe.resources.location=bpmn
# Enable zeebe Dev Service:
quarkus.zeebe.devservices.enabled=true
# only start devservices, if no running docker container is found
quarkus.zeebe.devservices.shared=true
# zeebe service name
quarkus.zeebe.devservices.service-name=zeebe_broker
# enable reusable zeebe test-container (https://www.testcontainers.org/features/reuse/)
quarkus.zeebe.devservices.reuse=false
# enable zeebe monitor Dev Service:
quarkus.zeebe.devservices.monitor.enabled=true
# zeebe monitor service name
quarkus.zeebe.devservices.monitor.service-name=zeebe-dev-monitor
# enable reusable zeebe test-container (https://www.testcontainers.org/features/reuse/)
quarkus.zeebe.devservices.monitor.reuse=false
```

#### Shared local dev instance

```
quarkus.zeebe.client.broker.gateway-address=localhost:26500
# If you are sure that there is already an instance running, yu can directly deactivate it
quarkus.zeebe.devservices.enabled=false
quarkus.zeebe.devservices.shared=true
quarkus.zeebe.devservices.monitor.serviceName=zeebe-dev-monitor
quarkus.zeebe.devservices.serviceName=zeebe_broker
```

#### Direct interaction with Camunda live instance
Preferably you would be using a dev instance of Camunda and not your production process engine ;)

```
# Disable local dev services
quarkus.zeebe.devservices.enabled=false

# Enter your cloud credentials from the zeebe portal
quarkus.zeebe.client.broker.gateway-address=
# cloud configuration
quarkus.zeebe.client.cloud.cluster-id=
quarkus.zeebe.client.cloud.client-id=
quarkus.zeebe.client.cloud.client-secret=
quarkus.zeebe.client.cloud.region=
quarkus.zeebe.client.cloud.base-url=zeebe.camunda.io
quarkus.zeebe.client.cloud.auth-url=https://login.cloud.camunda.io/oauth/token
quarkus.zeebe.client.cloud.port=443

# Make sure you are disabling plaintext security, otherwise connection will fail
quarkus.zeebe.client.security.plaintext=false
```

## Tracing

Whether `zeebe` tracing is enabled or not is done by `quarkus.zeebe.tracing.enabled` build time property. The default is `true`, but shown here to indicate how it can be disabled.
```properties
quarkus.zeebe.tracing.enabled=true
```
![OpenTelemetry](docs/images/opentelemetry.png)

### OpenTelemetry

If you already have your Quarkus project configured, you can add the `quarkus-opentelemetry-exporter-otlp` extension to your project.
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-opentelemetry-exporter-otlp</artifactId>
</dependency>
```

[Zeebe example](examples/opentelemetry)  
[Quarkus OpenTelemetry](https://quarkus.io/guides/opentelemetry)

### OpenTracing

If you already have your Quarkus project configured, you can add the `smallrye-opentracing` extension to your project.
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-opentracing</artifactId>
</dependency>
```

[Zeebe example](examples/opentracing)  
[Quarkus OpenTracing](https://quarkus.io/guides/opentracing)

## Dev-Services
Dev Services for Zeebe is automatically enabled unless:
* `quarkus.zeebe.devservices.enabled` is set to false
* `quarkus.zeebe.broker.gateway-address` is configured

Dev Service for Zeebe relies on Docker to start the broker. If your environment does not support Docker, you will need 
to start the broker manually, or connect to an already running broker. You can configure the broker address using 
`quarkus.zeebe.broker.gateway-address`.

![Test](docs/images/devservice.svg)

To activate [Zeebe-Dev-Monitor](https://github.com/lorislab/zeebe-dev-monitor) Dev Service use this configuration:
```properties
quarkus.zeebe.devservices.enabled=true
quarkus.zeebe.devservices.monitor.enabled=true
```
Property `qquarkus.zeebe.devservices.monitor.enabled=true` will activate the debug exporter.

#### Configuration

```properties
quarkus.zeebe.devservices.enabled=true|false
quarkus.zeebe.devservices.port=
quarkus.zeebe.devservices.shared=true
quarkus.zeebe.devservices.service-name=zeebe
quarkus.zeebe.devservices.image-name=
# zeebe dev monitor dev-service
quarkus.zeebe.devservices.monitor.enabled=true|false
quarkus.zeebe.devservices.monitor.port=
quarkus.zeebe.devservices.monitor.image-name=ghcr.io/lorislab/zeebe-dev-monitor:1.0.0
quarkus.zeebe.devservices.monitor.service-name=zeebe-dev-monitor
```

## Simple usage

```java

public class Job1Worker {

    @JobWorker(type = "job1")
    public void job1(@VariablesAsType Parameter p) {
        
    }
}
```

## Testing 

To use the test extension, add this dependency to the project:
```xml
<dependency>
    <groupId>io.quarkiverse.zeebe</groupId>
    <artifactId>quarkus-zeebe-test</artifactId>
    <version>{version}</version>
    <scope>test</scope>
</dependency>
```
![Test](./docs/images/testing.svg)

To use the `ZeebeClient` and `BpmnAssert` in the tests use the `@QuarkusTestResource(ZeebeTestResource.class)` and enable this configuration:
```properties
quarkus.zeebe.devservices.enabled=true
```
Test example
```java
import io.quarkiverse.zeebe.test.ZeebeTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.camunda.zeebe.client.ZeebeClient;

@QuarkusTest
@QuarkusTestResource(ZeebeTestResource.class)
public class BaseTest extends AbstractTest {

    @InjectZeebeClient
    ZeebeClient client;

    @Test
    public void startProcessTest() {
        ProcessInstanceEvent event = client.newCreateInstanceCommand()
                .bpmnProcessId("test").latestVersion()
                .variables(Map.of("k","v")).send().join();

        ProcessInstanceAssert a = BpmnAssert.assertThat(event);
        await().atMost(7, SECONDS).untilAsserted(a::isCompleted);
    }
}
```
We can reuse the test for the integration test.
```java
import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
public class BaseIT extends BaseTest {

}
```
For more information check examples in the `integration-tests` directory in this repo.

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tr>
    <td align="center"><a href="https://www.lorislab.org"><img src="https://avatars2.githubusercontent.com/u/828045?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Andrej Petras</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkiverse-zeebe/commits?author=andrejpetras" title="Code">💻</a> <a href="#maintenance-andrejpetras" title="Maintenance">🚧</a></td>
  </tr>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification.
Contributions of any kind welcome!
