package io.quarkiverse.zeebe.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import io.camunda.zeebe.client.ZeebeClientConfiguration;
import io.camunda.zeebe.client.api.JsonMapper;
import io.camunda.zeebe.client.api.command.ActivateJobsCommandStep1;
import io.camunda.zeebe.client.api.command.CancelProcessInstanceCommandStep1;
import io.camunda.zeebe.client.api.command.CompleteJobCommandStep1;
import io.camunda.zeebe.client.api.command.CreateProcessInstanceCommandStep1;
import io.camunda.zeebe.client.api.command.DeployProcessCommandStep1;
import io.camunda.zeebe.client.api.command.FailJobCommandStep1;
import io.camunda.zeebe.client.api.command.PublishMessageCommandStep1;
import io.camunda.zeebe.client.api.command.ResolveIncidentCommandStep1;
import io.camunda.zeebe.client.api.command.SetVariablesCommandStep1;
import io.camunda.zeebe.client.api.command.ThrowErrorCommandStep1;
import io.camunda.zeebe.client.api.command.TopologyRequestStep1;
import io.camunda.zeebe.client.api.command.UpdateRetriesJobCommandStep1;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobWorkerBuilderStep1;
import io.quarkiverse.zeebe.ZeebeClientInterceptor;
import io.quarkus.runtime.ShutdownEvent;

@ApplicationScoped
public class ZeebeClientService implements ZeebeClient {

    private static final Logger log = Logger.getLogger(ZeebeClientService.class);

    ZeebeRuntimeConfig config;

    ZeebeClient client;

    @Inject
    JsonMapper jsonMapper;

    @Inject
    @Any
    Instance<ZeebeClientInterceptor> interceptors;

    void initialize(ZeebeRuntimeConfig config) {
        log.debugf("Creating new zeebe client for %s", config.broker.gatewayAddress);
        this.config = config;
        ZeebeClientBuilder builder = ZeebeClientBuilderFactory.createBuilder(config);
        if (jsonMapper != null) {
            builder.withJsonMapper(jsonMapper);
        }
        interceptors.forEach(x -> builder.withInterceptors(x::interceptCall));
        client = builder.build();
    }

    void onStop(@Observes ShutdownEvent event) {
        close();
    }

    @Override
    public TopologyRequestStep1 newTopologyRequest() {
        return client.newTopologyRequest();
    }

    @Override
    public ZeebeClientConfiguration getConfiguration() {
        return client.getConfiguration();
    }

    @Override
    public void close() {
        client.close();
    }

    @Override
    public DeployProcessCommandStep1 newDeployCommand() {
        return client.newDeployCommand();
    }

    @Override
    public CreateProcessInstanceCommandStep1 newCreateInstanceCommand() {
        return client.newCreateInstanceCommand();
    }

    @Override
    public CancelProcessInstanceCommandStep1 newCancelInstanceCommand(long processInstanceKey) {
        return client.newCancelInstanceCommand(processInstanceKey);
    }

    @Override
    public SetVariablesCommandStep1 newSetVariablesCommand(long elementInstanceKey) {
        return client.newSetVariablesCommand(elementInstanceKey);
    }

    @Override
    public PublishMessageCommandStep1 newPublishMessageCommand() {
        return client.newPublishMessageCommand();
    }

    @Override
    public ResolveIncidentCommandStep1 newResolveIncidentCommand(long incidentKey) {
        return client.newResolveIncidentCommand(incidentKey);
    }

    @Override
    public UpdateRetriesJobCommandStep1 newUpdateRetriesCommand(long jobKey) {
        return client.newUpdateRetriesCommand(jobKey);
    }

    @Override
    public UpdateRetriesJobCommandStep1 newUpdateRetriesCommand(ActivatedJob job) {
        return client.newUpdateRetriesCommand(job);
    }

    @Override
    public JobWorkerBuilderStep1 newWorker() {
        return client.newWorker();
    }

    @Override
    public ActivateJobsCommandStep1 newActivateJobsCommand() {
        return client.newActivateJobsCommand();
    }

    @Override
    public CompleteJobCommandStep1 newCompleteCommand(long jobKey) {
        return client.newCompleteCommand(jobKey);
    }

    @Override
    public CompleteJobCommandStep1 newCompleteCommand(ActivatedJob job) {
        return client.newCompleteCommand(job);
    }

    @Override
    public FailJobCommandStep1 newFailCommand(long jobKey) {
        return client.newFailCommand(jobKey);
    }

    @Override
    public FailJobCommandStep1 newFailCommand(ActivatedJob job) {
        return client.newFailCommand(job);
    }

    @Override
    public ThrowErrorCommandStep1 newThrowErrorCommand(long jobKey) {
        return client.newThrowErrorCommand(jobKey);
    }

    @Override
    public ThrowErrorCommandStep1 newThrowErrorCommand(ActivatedJob job) {
        return client.newThrowErrorCommand(job);
    }
}
