package io.quarkiverse.zeebe.runtime.noop;

import io.camunda.zeebe.client.api.search.filter.UserTaskFilter;
import io.camunda.zeebe.client.api.search.query.UserTaskQuery;
import io.camunda.zeebe.client.api.search.response.UserTask;
import io.camunda.zeebe.client.api.search.sort.UserTaskSort;

public class UserTaskQueryImpl extends AbstractQuery<UserTask, UserTaskQuery, UserTaskFilter, UserTaskSort>
        implements UserTaskQuery {

}
