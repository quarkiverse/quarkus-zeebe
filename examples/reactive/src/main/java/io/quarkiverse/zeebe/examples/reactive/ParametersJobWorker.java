package io.quarkiverse.zeebe.examples.reactive;

import java.util.UUID;

import io.quarkiverse.zeebe.JobWorker;
import io.quarkiverse.zeebe.Variable;
import io.quarkiverse.zeebe.VariablesAsType;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;

public class ParametersJobWorker {

    @JobWorker(type = "create-param")
    public Uni<Parameter> createParam(@Variable("_info") String info) {
        Parameter p = new Parameter();
        p.info = info;
        p.data = UUID.randomUUID().toString();
        return Uni.createFrom().item(p);
    }

    @JobWorker(type = "update-param")
    public Uni<Parameter> updateParam(@VariablesAsType Parameter parameter) {
        parameter.data = "Hi, " + parameter.info;
        return Uni.createFrom().item(parameter);
    }

    @JobWorker(type = "info-param")
    public Uni<Void> infoParam(@VariablesAsType Parameter parameter) {
        Log.infof("%s", parameter);
        return Uni.createFrom().voidItem();
    }
}
