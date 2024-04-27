package io.quarkiverse.zeebe.runtime.devmode;

public class NotificationService {

    public void sendEvent(ProcessEvent data) {
        sendEvent(new NotificationEvent(NotificationEventType.PROCESS, data));
    }

    public void sendEvent(InstanceEvent data) {
        sendEvent(new NotificationEvent(NotificationEventType.PROCESS_INSTANCE, data));
    }

    public void sendEvent(ClusterEvent data) {
        sendEvent(new NotificationEvent(NotificationEventType.CLUSTER, data));
    }

    private void sendEvent(NotificationEvent data) {

    }

    public record ProcessEvent(ProcessEventType type) {
    }

    public enum ProcessEventType {
        DEPLOYED;
    }

    public record ClusterEvent(String message, ClusterEventType type) {
    }

    public enum ClusterEventType {
        ERROR;
    }

    public record NotificationEvent(NotificationEventType type, Object data) {
    }

    public enum NotificationEventType {
        PROCESS,
        PROCESS_INSTANCE,
        CLUSTER;
    }

    public record InstanceEvent(long processInstanceKey, long processDefinitionKey, ProcessInstanceEventType type) {
    }

    public enum ProcessInstanceEventType {
        UPDATED,
        CREATED,
        REMOVED;
    }

}
