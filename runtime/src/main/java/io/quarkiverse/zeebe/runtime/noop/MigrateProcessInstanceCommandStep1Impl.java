package io.quarkiverse.zeebe.runtime.noop;

import io.camunda.zeebe.client.api.command.MigrateProcessInstanceCommandStep1;
import io.camunda.zeebe.client.api.command.MigrationPlan;
import io.camunda.zeebe.client.api.response.MigrateProcessInstanceResponse;
import io.camunda.zeebe.client.impl.response.MigrateProcessInstanceResponseImpl;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass;

public class MigrateProcessInstanceCommandStep1Impl extends AbstractStep<MigrateProcessInstanceResponse>
        implements MigrateProcessInstanceCommandStep1.MigrateProcessInstanceCommandFinalStep,
        MigrateProcessInstanceCommandStep1, MigrateProcessInstanceCommandStep1.MigrateProcessInstanceCommandStep2 {

    @Override
    public MigrateProcessInstanceCommandStep2 migrationPlan(long targetProcessDefinitionKey) {
        return this;
    }

    @Override
    public MigrateProcessInstanceCommandFinalStep migrationPlan(MigrationPlan migrationPlan) {
        return this;
    }

    @Override
    public MigrateProcessInstanceCommandFinalStep addMappingInstruction(String sourceElementId, String targetElementId) {
        return this;
    }

    @Override
    protected MigrateProcessInstanceResponse create() {
        return new MigrateProcessInstanceResponseImpl(GatewayOuterClass.MigrateProcessInstanceResponse.getDefaultInstance());
    }

    @Override
    public MigrateProcessInstanceCommandStep1 useRest() {
        return this;
    }

    @Override
    public MigrateProcessInstanceCommandStep1 useGrpc() {
        return this;
    }

    @Override
    public MigrateProcessInstanceCommandFinalStep operationReference(long operationReference) {
        return this;
    }
}
