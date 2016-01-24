package net.nemerosa.ontrack.job.support;

import net.nemerosa.ontrack.job.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultJobPortal implements JobPortal, Job {

    private final Logger logger = LoggerFactory.getLogger(JobPortal.class);

    private final JobScheduler jobScheduler;

    private final Collection<JobProvider> jobProviders = new ArrayList<>();

    public DefaultJobPortal(JobScheduler jobScheduler, Schedule schedule) {
        this.jobScheduler = jobScheduler;
        this.jobScheduler.schedule(this, schedule);
    }

    @Override
    public void registerJobProvider(JobProvider jobProvider) {
        jobProviders.add(jobProvider);
    }

    @Override
    public JobKey getKey() {
        return new JobKey("job", "portal");
    }

    @Override
    public Runnable getTask() {
        return () -> {
            logger.debug("[job-portal] Running the registration");
            for (JobProvider jobProvider : jobProviders) {
                register(jobProvider);
            }
        };
    }

    protected void register(JobProvider jobProvider) {
        logger.debug("[job-portal] Running the registration for {}", jobProvider);
        Collection<JobDefinition> jobDefinitions = jobProvider.getJobs();
        // Get all keys
        Set<JobKey> jobKeys = jobDefinitions.stream().map(definition -> definition.getJob().getKey()).collect(Collectors.toSet());
        // Get keys of jobs being already registered
        Collection<JobKey> scheduledKeys = jobScheduler.getJobKeysOfType(jobProvider.getType());
        // Gets the keys of the jobs not being scheduled yet
        Set<JobKey> jobToUnschedule = new HashSet<>(scheduledKeys);
        jobToUnschedule.removeAll(jobKeys);
        // Removes the unscheduled jobs
        jobToUnschedule.forEach(jobScheduler::unschedule);
        // Reschedule jobs (only when different schedule)
        jobDefinitions.forEach(this::reschedule);
    }

    protected void reschedule(JobDefinition jobDefinition) {
        JobKey key = jobDefinition.getJob().getKey();
        JobStatus jobStatus = jobScheduler.getJobStatus(key);
        if (jobStatus.getSchedule().sameDelayThan(jobDefinition.getSchedule())) {
            logger.debug("[job-portal] Not rescheduling {} - same schedule", key);
        } else {
            logger.debug("[job-portal] Rescheduling {} with schedule {}", key, jobDefinition.getSchedule());
            jobScheduler.schedule(jobDefinition.getJob(), jobDefinition.getSchedule());
        }
    }

    @Override
    public String getDescription() {
        return "Orchestration of jobs with the scheduler";
    }

    @Override
    public boolean isDisabled() {
        return false;
    }
}
