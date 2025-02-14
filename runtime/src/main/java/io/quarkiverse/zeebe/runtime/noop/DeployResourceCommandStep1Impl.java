package io.quarkiverse.zeebe.runtime.noop;

import java.io.InputStream;
import java.nio.charset.Charset;

import io.camunda.zeebe.client.api.command.DeployResourceCommandStep1;
import io.camunda.zeebe.client.api.response.*;
import io.camunda.zeebe.client.impl.response.DeploymentEventImpl;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;

public class DeployResourceCommandStep1Impl extends AbstractStep<DeploymentEvent>
        implements DeployResourceCommandStep1, DeployResourceCommandStep1.DeployResourceCommandStep2 {

    @Override
    public DeployResourceCommandStep2 addResourceBytes(byte[] resourceBytes, String resourceName) {
        return this;
    }

    @Override
    public DeployResourceCommandStep2 addResourceString(String resourceString, Charset charset, String resourceName) {
        return this;
    }

    @Override
    public DeployResourceCommandStep2 addResourceStringUtf8(String resourceString, String resourceName) {
        return this;
    }

    @Override
    public DeployResourceCommandStep2 addResourceStream(InputStream resourceStream, String resourceName) {
        return this;
    }

    @Override
    public DeployResourceCommandStep2 addResourceFromClasspath(String classpathResource) {
        return this;
    }

    @Override
    public DeployResourceCommandStep2 addResourceFile(String filename) {
        return this;
    }

    @Override
    public DeployResourceCommandStep2 addProcessModel(BpmnModelInstance processDefinition, String resourceName) {
        return this;
    }

    @Override
    public DeploymentEvent create() {
        return new DeploymentEventImpl(GatewayOuterClass.DeployResourceResponse.getDefaultInstance());
    }

    @Override
    public DeployResourceCommandStep2 tenantId(String tenantId) {
        return this;
    }

    @Override
    public DeployResourceCommandStep1 useRest() {
        return this;
    }

    @Override
    public DeployResourceCommandStep1 useGrpc() {
        return this;
    }
}
