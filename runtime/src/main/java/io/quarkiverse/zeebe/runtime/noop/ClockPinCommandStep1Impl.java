package io.quarkiverse.zeebe.runtime.noop;

import java.time.Instant;

import io.camunda.zeebe.client.api.command.ClockPinCommandStep1;
import io.camunda.zeebe.client.api.response.PinClockResponse;

public class ClockPinCommandStep1Impl extends AbstractStep<PinClockResponse> implements ClockPinCommandStep1 {
    @Override
    public ClockPinCommandStep1 time(long timestamp) {
        return this;
    }

    @Override
    public ClockPinCommandStep1 time(Instant instant) {
        return this;
    }
}
