package net.nemerosa.ontrack.job.support;

import net.nemerosa.ontrack.common.Time;
import net.nemerosa.ontrack.job.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class DefaultJobScheduler implements JobScheduler {

    private final Logger logger = LoggerFactory.getLogger(JobScheduler.class);

    private final JobDecorator jobDecorator;
    private final ScheduledExecutorService scheduledExecutorService;

    private final Map<JobKey, JobScheduledService> services = new ConcurrentHashMap<>(new TreeMap<>());
    private final AtomicBoolean schedulerPaused = new AtomicBoolean(false);

    public DefaultJobScheduler(JobDecorator jobDecorator, ScheduledExecutorService scheduledExecutorService) {
        this.jobDecorator = jobDecorator;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public void schedule(Job job, Schedule schedule) {
        logger.info("[job][{}][{}] Scheduling with {}", job.getKey().getType(), job.getKey().getId(), schedule);
        // Manages existing schedule
        JobScheduledService existingService = services.remove(job.getKey());
        if (existingService != null) {
            logger.info("[job][{}][{}] Stopping existing schedule", job.getKey().getType(), job.getKey().getId());
            existingService.cancel(false);
        }
        // Gets the job task
        Runnable jobTask = job.getTask();
        // Decorates the task
        Runnable decoratedTask = jobDecorator.decorate(job, jobTask);
        // Creates and starts the scheduled service
        logger.info("[job][{}][{}] Starting service", job.getKey().getType(), job.getKey().getId());
        // Copy stats from old schedule
        JobScheduledService jobScheduledService = new JobScheduledService(job, decoratedTask, schedule, scheduledExecutorService, existingService);
        // Registration
        services.put(job.getKey(), jobScheduledService);
    }

    @Override
    public void unschedule(JobKey key) {
        JobScheduledService existingService = services.remove(key);
        if (existingService != null) {
            existingService.cancel(true);
        }
    }

    @Override
    public void pause() {
        schedulerPaused.set(true);
    }

    @Override
    public void resume() {
        schedulerPaused.set(false);
    }

    @Override
    public void pause(JobKey key) {
        JobScheduledService existingService = services.get(key);
        if (existingService != null) {
            existingService.pause();
        } else {
            throw new JobNotScheduledException(key);
        }
    }

    @Override
    public void resume(JobKey key) {
        JobScheduledService existingService = services.get(key);
        if (existingService != null) {
            existingService.resume();
        } else {
            throw new JobNotScheduledException(key);
        }
    }

    @Override
    public JobStatus getJobStatus(JobKey key) {
        JobScheduledService existingService = services.get(key);
        if (existingService != null) {
            return existingService.getJobStatus();
        } else {
            throw new JobNotScheduledException(key);
        }
    }

    @Override
    public Collection<JobKey> getAllJobKeys() {
        return services.keySet();
    }

    @Override
    public Collection<JobKey> getJobKeysOfType(String type) {
        return getAllJobKeys().stream()
                .filter(key -> key.sameType(type))
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<JobStatus> getJobStatuses() {
        return services.values().stream()
                .map(JobScheduledService::getJobStatus)
                .collect(Collectors.toList());
    }

    @Override
    public Future<?> fireImmediately(JobKey jobKey) {
        // Gets the existing scheduled service
        JobScheduledService jobScheduledService = services.get(jobKey);
        if (jobScheduledService == null) {
            throw new JobNotScheduledException(jobKey);
        }
        // Fires the job immediately
        return jobScheduledService.fireImmediately();
    }

    private class JobScheduledService implements Runnable {

        private final Job job;
        private final Schedule schedule;
        private final Runnable monitoredTask;
        private final ScheduledFuture<?> scheduledFuture;

        private final AtomicBoolean paused = new AtomicBoolean(false);
        private final AtomicReference<CompletableFuture<?>> completableFuture = new AtomicReference<>();

        private final AtomicLong runCount = new AtomicLong();
        private final AtomicReference<LocalDateTime> lastRunDate = new AtomicReference<>();
        private final AtomicLong lastRunDurationMs = new AtomicLong();
        private final AtomicLong lastErrorCount = new AtomicLong();
        private final AtomicReference<String> lastError = new AtomicReference<>(null);

        private JobScheduledService(Job job, Runnable decoratedTask, Schedule schedule, ScheduledExecutorService scheduledExecutorService, JobScheduledService old) {
            this.job = job;
            this.schedule = schedule;
            this.monitoredTask = new MonitoredTask(decoratedTask);
            // Copies stats from old service
            if (old != null) {
                runCount.set(old.runCount.get());
                lastRunDate.set(old.lastRunDate.get());
                lastRunDurationMs.set(old.lastRunDurationMs.get());
                lastErrorCount.set(old.lastErrorCount.get());
                lastError.set(old.lastError.get());
            }
            // Scheduling now
            scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(
                    this,
                    schedule.getInitialPeriod(),
                    schedule.getPeriod(),
                    schedule.getUnit()
            );
        }

        @Override
        public void run() {
            fireImmediately();
        }

        public void cancel(boolean forceStop) {
            scheduledFuture.cancel(false);
            // The decorated task might still run
            CompletableFuture<?> future = this.completableFuture.get();
            if (forceStop && future != null && !future.isDone() && !future.isCancelled()) {
                future.cancel(true);
            }
        }

        public Future<?> fireImmediately() {
            return completableFuture.updateAndGet(this::optionallyFireTask);
        }

        protected CompletableFuture<?> optionallyFireTask(CompletableFuture<?> runningCompletableFuture) {
            if (runningCompletableFuture != null) {
                return runningCompletableFuture;
            } else {
                return fireTask();
            }
        }

        protected CompletableFuture<Void> fireTask() {
            return CompletableFuture
                    .runAsync(monitoredTask, scheduledExecutorService)
                    .whenComplete((ignored, ex) -> completableFuture.set(null));
        }

        public JobStatus getJobStatus() {
            return new JobStatus(
                    job.getKey(),
                    schedule,
                    job.getDescription(),
                    completableFuture.get() != null,
                    runCount.get(),
                    lastRunDate.get(),
                    lastRunDurationMs.get(),
                    getNextRunDate(),
                    lastErrorCount.get(),
                    lastError.get()
            );
        }

        private LocalDateTime getNextRunDate() {
            LocalDateTime date = lastRunDate.get();
            if (date != null) {
                return date.plus(schedule.toMiliseconds(), ChronoUnit.MILLIS);
            } else {
                return null;
            }
        }

        public void pause() {
            paused.set(true);
        }

        public void resume() {
            paused.set(false);
        }

        private class MonitoredTask implements Runnable {

            private final Runnable decoratedTask;

            public MonitoredTask(Runnable decoratedTask) {
                this.decoratedTask = decoratedTask;
            }

            @Override
            public void run() {
                if (isEnabled()) {
                    try {
                        logger.debug("[job][{}][{}] Running now", job.getKey().getType(), job.getKey().getId());
                        lastRunDate.set(Time.now());
                        runCount.incrementAndGet();
                        // Runs the job
                        long _start = System.currentTimeMillis();
                        decoratedTask.run();
                        // No error, counting time
                        long _end = System.currentTimeMillis();
                        lastRunDurationMs.set(_end - _start);
                        logger.debug("[job][{}][{}] Ran in {} ms", job.getKey().getType(), job.getKey().getId(), lastRunDurationMs.get());
                        // No error - resetting the counters
                        lastErrorCount.set(0);
                        lastError.set(null);
                    } catch (RuntimeException ex) {
                        lastErrorCount.incrementAndGet();
                        lastError.set(ex.getMessage());
                        logger.error("[job][{}][{}] Error: {}", job.getKey().getType(), job.getKey().getId(), ex.getMessage());
                        // TODO Error reporter
                        throw ex;
                    }
                } else {
                    logger.debug("[job][{}][{}] Not enabled", job.getKey().getType(), job.getKey().getId());
                }
            }
        }

        private boolean isEnabled() {
            return !job.isDisabled() && !paused.get() && !schedulerPaused.get();
        }
    }
}
