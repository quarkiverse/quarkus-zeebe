package io.quarkiverse.zeebe.runtime.devmode.models;

public final class UserTaskHeaders {

    public static final String JOB_TYPE = "io.camunda.zeebe:userTask";

    public static final String CANDIDATE_USERS = "io.camunda.zeebe:candidateUsers";
    public static final String CANDIDATE_GROUPS = "io.camunda.zeebe:candidateGroups";
    public static final String ASSIGNEE = "io.camunda.zeebe:assignee";
    public static final String DUE_DATE = "io.camunda.zeebe:dueDate";
    public static final String FOLLOW_UP_DATE = "io.camunda.zeebe:followUpDate";
}
