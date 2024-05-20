package io.quarkiverse.zeebe.runtime.noop;

import io.camunda.zeebe.client.api.command.ResolveIncidentCommandStep1;
import io.camunda.zeebe.client.api.response.ResolveIncidentResponse;

public class ResolveIncidentCommandStep1Impl extends AbstractStep<ResolveIncidentResponse>
        implements ResolveIncidentCommandStep1 {

    @Override
    protected ResolveIncidentResponse create() {
        return new ResolveIncidentResponse() {
        };
    }
}
