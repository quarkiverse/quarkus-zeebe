package io.quarkiverse.zeebe.test.records;

import java.util.List;
import java.util.stream.Collectors;

import io.camunda.zeebe.protocol.record.value.JobBatchRecordValue;
import io.camunda.zeebe.protocol.record.value.JobRecordValue;
import io.zeebe.exporter.proto.Schema;

public class JobBatchRecordValueImpl extends RecordValueImpl implements JobBatchRecordValue {

    private final Schema.JobBatchRecord record;

    private List<JobRecordValue> jobs;

    public JobBatchRecordValueImpl(Schema.JobBatchRecord record) {
        this.record = record;
    }

    @Override
    public String getType() {
        return record.getType();
    }

    @Override
    public String getWorker() {
        return record.getWorker();
    }

    @Override
    public long getTimeout() {
        return record.getTimeout();
    }

    @Override
    public int getMaxJobsToActivate() {
        return record.getMaxJobsToActivate();
    }

    @Override
    public List<Long> getJobKeys() {
        return record.getJobKeysList();
    }

    @Override
    public List<JobRecordValue> getJobs() {
        if (jobs == null) {
            jobs = record.getJobsList().stream()
                    .map(JobRecordValueImpl::new).collect(Collectors.toList());
        }
        return jobs;
    }

    @Override
    public boolean isTruncated() {
        return record.getTruncated();
    }

    @Override
    public Schema.RecordMetadata getMetadata() {
        return record.getMetadata();
    }

}
