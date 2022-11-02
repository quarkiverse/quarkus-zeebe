package io.quarkiverse.zeebe.runtime.tracing;

import static io.quarkiverse.zeebe.runtime.tracing.ZeebeTracing.DEPLOY_RESOURCES;
import static io.quarkiverse.zeebe.runtime.tracing.ZeebeTracing.FAIL_MESSAGE;
import static io.quarkiverse.zeebe.runtime.tracing.ZeebeTracing.INCIDENT_KEY;
import static io.quarkiverse.zeebe.runtime.tracing.ZeebeTracing.JOB_KEY;
import static io.quarkiverse.zeebe.runtime.tracing.ZeebeTracing.JOB_RETRIES;
import static io.quarkiverse.zeebe.runtime.tracing.ZeebeTracing.JOB_VARIABLES;
import static io.quarkiverse.zeebe.runtime.tracing.ZeebeTracing.MESSAGE_CORRELATION_KEY;
import static io.quarkiverse.zeebe.runtime.tracing.ZeebeTracing.MESSAGE_ID;
import static io.quarkiverse.zeebe.runtime.tracing.ZeebeTracing.MESSAGE_NAME;
import static io.quarkiverse.zeebe.runtime.tracing.ZeebeTracing.MESSAGE_TIME_TO_LIVE;
import static io.quarkiverse.zeebe.runtime.tracing.ZeebeTracing.MESSAGE_VARIABLES;
import static io.quarkiverse.zeebe.runtime.tracing.ZeebeTracing.PROCESS_DEF_KEY;
import static io.quarkiverse.zeebe.runtime.tracing.ZeebeTracing.PROCESS_DEF_VER;
import static io.quarkiverse.zeebe.runtime.tracing.ZeebeTracing.PROCESS_ELEMENT_INSTANCE_KEY;
import static io.quarkiverse.zeebe.runtime.tracing.ZeebeTracing.PROCESS_ID;
import static io.quarkiverse.zeebe.runtime.tracing.ZeebeTracing.PROCESS_INSTANCE_KEY;
import static io.quarkiverse.zeebe.runtime.tracing.ZeebeTracing.PROCESS_VARIABLES;
import static io.quarkiverse.zeebe.runtime.tracing.ZeebeTracing.PROCESS_VARIABLES_SCOPE;
import static io.quarkiverse.zeebe.runtime.tracing.ZeebeTracing.REQUEST_TIMEOUT;

import java.util.stream.Collectors;

import io.camunda.zeebe.gateway.protocol.GatewayOuterClass;
import io.grpc.ClientCall;
import io.grpc.ForwardingClientCall;

public abstract class ZeebeForwardingClient<ReqT, RespT> extends ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT> {

    public ZeebeForwardingClient(ClientCall<ReqT, RespT> delegate) {
        super(delegate);
    }

    @Override
    public void sendMessage(ReqT message) {

        // ignore active jobs request
        if (message instanceof GatewayOuterClass.ActivateJobsRequest) {
            super.sendMessage(message);
            return;
        }
        // ignore topology request
        if (message instanceof GatewayOuterClass.TopologyRequest) {
            super.sendMessage(message);
            return;
        }

        createTracingMessage(message);
    }

    protected abstract void createTracingMessage(ReqT message);

    protected void sendTracingMessage(ReqT message, AttributeCallback span, AttributeCallback callSpan) {
        if (message instanceof GatewayOuterClass.CompleteJobRequest) {
            GatewayOuterClass.CompleteJobRequest r = (GatewayOuterClass.CompleteJobRequest) message;
            callSpan.setAttribute(JOB_KEY, r.getJobKey())
                    .setAttribute(JOB_VARIABLES, r.getVariables());

        } else if (message instanceof GatewayOuterClass.CreateProcessInstanceRequest) {
            message = createProcessInstance(message);
            GatewayOuterClass.CreateProcessInstanceRequest r = (GatewayOuterClass.CreateProcessInstanceRequest) message;
            callSpan.setAttribute(PROCESS_DEF_KEY, r.getProcessDefinitionKey())
                    .setAttribute(PROCESS_ID, r.getBpmnProcessId())
                    .setAttribute(PROCESS_DEF_VER, r.getVersion())
                    .setAttribute(PROCESS_VARIABLES, r.getVariables());

        } else if (message instanceof GatewayOuterClass.FailJobRequest) {
            GatewayOuterClass.FailJobRequest r = (GatewayOuterClass.FailJobRequest) message;
            span.setError().setAttribute(ZeebeTracing.FAIL_MESSAGE, r.getErrorMessage());

            callSpan.setAttribute(JOB_KEY, r.getJobKey())
                    .setAttribute(JOB_RETRIES, r.getRetries())
                    .setAttribute(FAIL_MESSAGE, r.getErrorMessage());

        } else if (message instanceof GatewayOuterClass.ThrowErrorRequest) {
            GatewayOuterClass.ThrowErrorRequest r = (GatewayOuterClass.ThrowErrorRequest) message;
            span.setError().setAttribute(ZeebeTracing.THROW_ERROR_MESSAGE, r.getErrorMessage())
                    .setAttribute(ZeebeTracing.THROW_ERROR_CODE, r.getErrorCode());

            callSpan.setAttribute(JOB_KEY, r.getJobKey())
                    .setAttribute(ZeebeTracing.THROW_ERROR_MESSAGE, r.getErrorMessage())
                    .setAttribute(ZeebeTracing.THROW_ERROR_CODE, r.getErrorCode());

        } else if (message instanceof GatewayOuterClass.CreateProcessInstanceWithResultRequest) {
            message = createProcessInstanceWithResult(message);
            GatewayOuterClass.CreateProcessInstanceWithResultRequest rr = (GatewayOuterClass.CreateProcessInstanceWithResultRequest) message;
            GatewayOuterClass.CreateProcessInstanceRequest r = rr.getRequest();
            callSpan.setAttribute(REQUEST_TIMEOUT, rr.getRequestTimeout())
                    .setAttribute(PROCESS_DEF_KEY, r.getProcessDefinitionKey())
                    .setAttribute(PROCESS_ID, r.getBpmnProcessId())
                    .setAttribute(PROCESS_DEF_VER, r.getVersion())
                    .setAttribute(PROCESS_VARIABLES, r.getVariables());

        } else if (message instanceof GatewayOuterClass.PublishMessageRequest) {
            GatewayOuterClass.PublishMessageRequest r = (GatewayOuterClass.PublishMessageRequest) message;
            callSpan.setAttribute(MESSAGE_CORRELATION_KEY, r.getCorrelationKey())
                    .setAttribute(MESSAGE_ID, r.getMessageId())
                    .setAttribute(MESSAGE_NAME, r.getName())
                    .setAttribute(MESSAGE_TIME_TO_LIVE, r.getTimeToLive())
                    .setAttribute(MESSAGE_VARIABLES, r.getVariables());

        } else if (message instanceof GatewayOuterClass.CancelProcessInstanceRequest) {
            GatewayOuterClass.CancelProcessInstanceRequest r = (GatewayOuterClass.CancelProcessInstanceRequest) message;
            callSpan.setAttribute(PROCESS_INSTANCE_KEY, r.getProcessInstanceKey());
        } else if (message instanceof GatewayOuterClass.DeployResourceRequest) {
            GatewayOuterClass.DeployResourceRequest r = (GatewayOuterClass.DeployResourceRequest) message;
            String tmp = r.getResourcesList()
                    .stream()
                    .map(GatewayOuterClass.Resource::getName)
                    .collect(Collectors.joining(","));
            callSpan.setAttribute(DEPLOY_RESOURCES, tmp);
        } else if (message instanceof GatewayOuterClass.ResolveIncidentRequest) {
            GatewayOuterClass.ResolveIncidentRequest r = (GatewayOuterClass.ResolveIncidentRequest) message;
            callSpan.setAttribute(INCIDENT_KEY, r.getIncidentKey());

        } else if (message instanceof GatewayOuterClass.SetVariablesRequest) {
            GatewayOuterClass.SetVariablesRequest r = (GatewayOuterClass.SetVariablesRequest) message;
            callSpan.setAttribute(PROCESS_ELEMENT_INSTANCE_KEY, r.getElementInstanceKey())
                    .setAttribute(PROCESS_VARIABLES, r.getVariables())
                    .setAttribute(PROCESS_VARIABLES_SCOPE, r.getLocal());

        } else if (message instanceof GatewayOuterClass.UpdateJobRetriesRequest) {
            GatewayOuterClass.UpdateJobRetriesRequest r = (GatewayOuterClass.UpdateJobRetriesRequest) message;
            callSpan.setAttribute(JOB_KEY, r.getJobKey()).setAttribute(JOB_RETRIES, r.getRetries());
        }

        super.sendMessage(message);
    }

    private <ReqT> ReqT createProcessInstance(ReqT message) {
        @SuppressWarnings("unchecked")
        ReqT request = (ReqT) convert((GatewayOuterClass.CreateProcessInstanceRequest) message);
        return request;
    }

    private <ReqT> ReqT createProcessInstanceWithResult(ReqT message) {
        GatewayOuterClass.CreateProcessInstanceWithResultRequest.Builder resultBuilder = GatewayOuterClass.CreateProcessInstanceWithResultRequest
                .newBuilder((GatewayOuterClass.CreateProcessInstanceWithResultRequest) message);
        resultBuilder.setRequest(convert(resultBuilder.getRequest()));
        @SuppressWarnings("unchecked")
        ReqT request = (ReqT) resultBuilder.build();
        return request;
    }

    abstract GatewayOuterClass.CreateProcessInstanceRequest convert(GatewayOuterClass.CreateProcessInstanceRequest request);

    interface AttributeCallback {

        AttributeCallback setError();

        AttributeCallback setAttribute(String key, String value);

        AttributeCallback setAttribute(String key, int value);

        AttributeCallback setAttribute(String key, long value);

        AttributeCallback setAttribute(String key, boolean value);
    }
}
