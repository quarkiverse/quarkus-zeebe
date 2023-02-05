package io.quarkiverse.zeebe;

import java.util.concurrent.CompletionStage;

import org.jboss.jandex.DotName;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.smallrye.common.annotation.NonBlocking;

public class ZeebeDotNames {

    static final DotName JOB_WORKER = DotName.createSimple(JobWorker.class.getName());

    static final DotName NON_BLOCKING = DotName.createSimple(NonBlocking.class.getName());
    static final DotName UNI = DotName.createSimple("io.smallrye.mutiny.Uni");
    static final DotName COMPLETION_STAGE = DotName.createSimple(CompletionStage.class.getName());
    static final DotName VOID = DotName.createSimple(Void.class.getName());

    static final DotName JOB_CLIENT = DotName.createSimple(JobClient.class.getName());

    static final DotName ACTIVATED_JOB = DotName.createSimple(ActivatedJob.class.getName());

    static final DotName VARIABLE = DotName.createSimple(Variable.class.getName());

    static final DotName VARIABLE_AS_TYPE = DotName.createSimple(VariablesAsType.class.getName());

    static final DotName CUSTOM_HEADERS = DotName.createSimple(CustomHeaders.class.getName());

    static final DotName STRING = DotName.createSimple(String.class.getName());
}
