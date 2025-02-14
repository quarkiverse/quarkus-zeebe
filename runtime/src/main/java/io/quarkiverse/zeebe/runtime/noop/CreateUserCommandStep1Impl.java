package io.quarkiverse.zeebe.runtime.noop;

import io.camunda.zeebe.client.api.command.CreateUserCommandStep1;
import io.camunda.zeebe.client.api.response.CreateUserResponse;

public class CreateUserCommandStep1Impl extends AbstractStep<CreateUserResponse> implements CreateUserCommandStep1 {
    @Override
    public CreateUserCommandStep1 username(String username) {
        return this;
    }

    @Override
    public CreateUserCommandStep1 email(String email) {
        return this;
    }

    @Override
    public CreateUserCommandStep1 name(String name) {
        return this;
    }

    @Override
    public CreateUserCommandStep1 password(String password) {
        return this;
    }
}
