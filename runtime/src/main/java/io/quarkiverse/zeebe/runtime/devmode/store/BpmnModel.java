package io.quarkiverse.zeebe.runtime.devmode.store;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.model.bpmn.instance.CatchEvent;
import io.camunda.zeebe.model.bpmn.instance.ErrorEventDefinition;
import io.camunda.zeebe.model.bpmn.instance.SequenceFlow;
import io.camunda.zeebe.model.bpmn.instance.ServiceTask;
import io.camunda.zeebe.model.bpmn.instance.TimerEventDefinition;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeTaskDefinition;

public class BpmnModel {

    public static BpmnModelInstance loadModel(byte[] data) {
        return Bpmn.readModelFromStream(new ByteArrayInputStream(data));
    }

    public record BpmnElementInfo(String elementId, String info) {
    }

    public static List<BpmnElementInfo> loadBpmnElements(final BpmnModelInstance bpmn) {
        final List<BpmnElementInfo> infos = new ArrayList<>();

        bpmn.getModelElementsByType(ServiceTask.class)
                .forEach(x -> infos.add(new BpmnElementInfo(x.getId(),
                        "job-type: " + x.getSingleExtensionElement(ZeebeTaskDefinition.class).getType())));

        bpmn.getModelElementsByType(SequenceFlow.class)
                .forEach(x -> {
                    var conditionExpression = x.getConditionExpression();
                    if (conditionExpression != null && !conditionExpression.getTextContent().isEmpty()) {
                        infos.add(new BpmnElementInfo(x.getId(), "condition: " + conditionExpression.getTextContent()));
                    }
                });

        bpmn.getModelElementsByType(CatchEvent.class)
                .forEach(catchEvent -> catchEvent.getEventDefinitions()
                        .forEach(
                                x -> {
                                    if (x instanceof final ErrorEventDefinition errorEventDef) {
                                        infos.add(new BpmnElementInfo(catchEvent.getId(),
                                                "errorCode: " + errorEventDef.getError().getErrorCode()));
                                    }
                                    if (x instanceof final TimerEventDefinition timerEventDefinition) {
                                        Optional.<ModelElementInstance> ofNullable(timerEventDefinition.getTimeCycle())
                                                .or(() -> Optional.ofNullable(timerEventDefinition.getTimeDate()))
                                                .or(() -> Optional.ofNullable(timerEventDefinition.getTimeDuration()))
                                                .map(ModelElementInstance::getTextContent)
                                                .ifPresent(timer -> infos
                                                        .add(new BpmnElementInfo(catchEvent.getId(), "timer: " + timer)));
                                    }
                                }));

        return infos;
    }

}
