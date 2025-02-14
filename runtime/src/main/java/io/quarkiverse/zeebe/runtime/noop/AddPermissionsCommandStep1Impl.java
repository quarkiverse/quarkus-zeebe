package io.quarkiverse.zeebe.runtime.noop;

import java.util.List;

import io.camunda.zeebe.client.api.command.AddPermissionsCommandStep1;
import io.camunda.zeebe.client.api.response.AddPermissionsResponse;
import io.camunda.zeebe.client.protocol.rest.AuthorizationPatchRequest;
import io.camunda.zeebe.client.protocol.rest.AuthorizationPatchRequestPermissionsInner;

public class AddPermissionsCommandStep1Impl extends AbstractStep<AddPermissionsResponse>
        implements AddPermissionsCommandStep1, AddPermissionsCommandStep1.AddPermissionsCommandStep2,
        AddPermissionsCommandStep1.AddPermissionsCommandStep3, AddPermissionsCommandStep1.AddPermissionsCommandStep4 {

    @Override
    public AddPermissionsCommandStep2 resourceType(AuthorizationPatchRequest.ResourceTypeEnum resourceType) {
        return this;
    }

    @Override
    public AddPermissionsCommandStep3 permission(AuthorizationPatchRequestPermissionsInner.PermissionTypeEnum permissionType) {
        return this;
    }

    @Override
    public AddPermissionsCommandStep4 resourceIds(List<String> resourceIds) {
        return this;
    }

    @Override
    public AddPermissionsCommandStep4 resourceId(String resourceId) {
        return this;
    }
}
