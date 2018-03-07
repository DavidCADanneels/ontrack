package net.nemerosa.ontrack.job.support

import net.nemerosa.ontrack.common.Time
import net.nemerosa.ontrack.job.*
import org.apache.commons.lang3.Validate
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.function.BiFunction

class DefaultJobScheduler(
        private val jobDecorator: JobDecorator,
        private val schedulerPool: ScheduledExecutorService,
        private val jobListener: JobListener,
        initiallyPaused: Boolean,
        private val jobPoolProvider: BiFunction<ExecutorService, Job, ExecutorService>,
        private val scattering: Boolean,
        private val scatteringRatio: Double
) : JobScheduler {

    private val logger = LoggerFactory.getLogger(JobScheduler::class.java)

    private val services = ConcurrentHashMap(TreeMap<JobKey, JobScheduledService>())
    private val schedulerPaused: AtomicBoolean

    private val idGenerator = AtomicLong()

    constructor(
            jobDecorator: JobDecorator,
            schedulerPool: ScheduledExecutorService,
            jobListener: JobListener,
            initiallyPaused: Boolean,
            scattering: Boolean,
            scatteringRatio: Double
    ) : this(
            jobDecorator,
            schedulerPool,
            jobListener,
            initiallyPaused,
            BiFunction { executorService, _ -> executorService },
            scattering,
            scatteringRatio
    )

    init {
        Validate.inclusiveBetween(0.0, 1.0, scatteringRatio)
        this.schedulerPaused = AtomicBoolean(initiallyPaused)
    }

    override fun schedule(job: Job, schedule: Schedule) {
        logger.info("[scheduler][job]{} Scheduling with {}", job.key, schedule)
        // Manages existing schedule
        val existingService = services.remove(job.key)
        if (existingService != null) {
            logger.info("[scheduler][job]{} Stopping existing schedule", job.key)
            existingService.cancel(false)
        }
        // Creates and starts the scheduled service
        logger.info("[scheduler][job]{} Starting scheduled service", job.key)
        // Copy stats from old schedule
        val jobScheduledService = JobScheduledService(
                job,
                schedule,
                schedulerPool,
                existingService,
                jobListener.isPausedAtStartup(job.key)
        )
        // Registration
        services[job.key] = jobScheduledService
    }

    override fun unschedule(key: JobKey): Boolean {
        return unschedule(key, true)
    }

    private fun unschedule(key: JobKey, forceStop: Boolean): Boolean {
        logger.debug("[scheduler][job]{} Unscheduling job", key)
        val existingService = services.remove(key)
        return if (existingService != null) {
            logger.debug("[scheduler][job]{} Stopping running job", key)
            existingService.cancel(forceStop)
            true
        } else {
            false
        }
    }

    override fun pause() {
        schedulerPaused.set(true)
    }

    override fun resume() {
        schedulerPaused.set(false)
    }

    override fun isPaused(): Boolean {
        return schedulerPaused.get()
    }

    override fun pause(key: JobKey): Boolean {
        val existingService = services[key]
        if (existingService != null) {
            existingService.pause()
            return true
        } else {
            throw JobNotScheduledException(key)
        }
    }

    override fun resume(key: JobKey): Boolean {
        val existingService = services[key]
        if (existingService != null) {
            existingService.resume()
            return true
        } else {
            throw JobNotScheduledException(key)
        }
    }

    override fun getJobStatus(key: JobKey): Optional<JobStatus> {
        val existingService = services[key]
        return if (existingService != null) {
            Optional.of(existingService.jobStatus)
        } else {
            Optional.empty()
        }
    }

    override fun getJobKey(id: Long): Optional<JobKey> {
        return services.values.stream()
                .filter { service -> service.id == id }
                .map { it.jobKey }
                .findFirst()
    }

    override fun stop(key: JobKey): Boolean {
        val existingService = services[key]
        return existingService?.stop() ?: throw JobNotScheduledException(key)
    }

    override fun getAllJobKeys(): Collection<JobKey> {
        return services.keys
    }

    override fun getJobKeysOfType(type: JobType): Collection<JobKey> {
        return allJobKeys
                .filter { key -> key.sameType(type) }
                .toSet()
    }

    override fun getJobKeysOfCategory(category: JobCategory): Collection<JobKey> {
        return allJobKeys
                .filter { key -> key.sameCategory(category) }
                .toSet()
    }

    override fun getJobStatuses(): Collection<JobStatus> {
        return services.values
                .map { it.jobStatus }
                .sortedBy { it.id }
                .toList()
    }

    override fun fireImmediately(jobKey: JobKey): Optional<Future<*>> {
        // Gets the existing scheduled service
        val jobScheduledService = services[jobKey] ?: throw JobNotScheduledException(jobKey)
        // Fires the job immediately
        return jobScheduledService.doRun(true)
    }

    private fun getExecutorService(job: Job): ExecutorService {
        return jobPoolProvider.apply(schedulerPool, job)
    }

    private inner class JobScheduledService(
            private val job: Job,
            private val schedule: Schedule,
            scheduledExecutorService: ScheduledExecutorService,
            old: JobScheduledService?,
            pausedAtStartup: Boolean
    ) : Runnable {

        val id: Long = idGenerator.incrementAndGet()
        private val actualSchedule: Schedule
        private val scheduledFuture: ScheduledFuture<*>?

        private val paused: AtomicBoolean = AtomicBoolean(pausedAtStartup)

        private val currentExecution = AtomicReference<Future<*>>()
        private val runProgress = AtomicReference<JobRunProgress>()
        private val runCount = AtomicLong()
        private val lastRunDate = AtomicReference<LocalDateTime>()
        private val lastRunDurationMs = AtomicLong()
        private val lastErrorCount = AtomicLong()
        private val lastError = AtomicReference<String>(null)

        val jobKey: JobKey
            get() = job.key

        private val run: Runnable
            get() {
                val jobRunListener = DefaultJobRunListener()
                val rootTask = { job.task.run(jobRunListener) }
                val decoratedTask = jobDecorator.decorate(job, rootTask)
                val runnable = MonitoredRun(decoratedTask, object : MonitoredRunListenerAdapter() {
                    override fun onCompletion() {
                        logger.debug("[job][task]{} Removed job execution", job.key)
                        currentExecution.set(null)
                    }
                })
                val monitoredRunListener = object : MonitoredRunListener {
                    override fun onStart() {
                        logger.debug("[job][task]{} On start", job.key)
                        lastRunDate.set(Time.now())
                        runCount.incrementAndGet()
                        jobListener.onJobStart(job.key)
                    }

                    override fun onSuccess(duration: Long) {
                        lastRunDurationMs.set(duration)
                        logger.debug("[job][task]{} Success in {} ms", job.key, duration)
                        jobListener.onJobEnd(job.key, duration)
                        lastErrorCount.set(0)
                        lastError.set(null)
                    }

                    override fun onFailure(ex: Exception) {
                        lastErrorCount.incrementAndGet()
                        lastError.set(ex.message)
                        logger.debug("[job][task]{} Failure: {}", job.key, ex.message)
                        jobListener.onJobError(jobStatus, ex)
                    }

                    override fun onCompletion() {
                        runProgress.set(null)
                        logger.debug("[job][task]{} Job completed.", job.key)
                        jobListener.onJobComplete(job.key)
                    }
                }
                return MonitoredRun(runnable, monitoredRunListener)
            }

        val jobStatus: JobStatus
            get() {
                val valid = job.isValid
                return JobStatus(
                        id,
                        job.key,
                        schedule,
                        actualSchedule,
                        job.description,
                        currentExecution.get() != null,
                        valid,
                        paused.get(),
                        job.isDisabled,
                        runProgress.get(),
                        runCount.get(),
                        lastRunDate.get(),
                        lastRunDurationMs.get(),
                        getNextRunDate(valid),
                        lastErrorCount.get(),
                        lastError.get()
                )
            }

        init {
            // Paused at startup
            if (pausedAtStartup) {
                logger.debug("[job]{} Job paused at startup", job.key)
            }
            // Copies stats from old service
            if (old != null) {
                runCount.set(old.runCount.get())
                lastRunDate.set(old.lastRunDate.get())
                lastRunDurationMs.set(old.lastRunDurationMs.get())
                lastErrorCount.set(old.lastErrorCount.get())
                lastError.set(old.lastError.get())
            }
            // Converting all units to milliseconds
            var initialPeriod = TimeUnit.MILLISECONDS.convert(schedule.initialPeriod, schedule.unit)
            val period = TimeUnit.MILLISECONDS.convert(schedule.period, schedule.unit)
            // Scattering
            if (scattering) {
                // Computes the hash for the job key
                val hash = Math.abs(job.key.toString().hashCode()) % 10000
                // Period to consider
                val scatteringMax = (period * scatteringRatio).toLong()
                if (scatteringMax > 0) {
                    // Modulo on the period
                    val delay = hash * scatteringMax / 10000
                    logger.debug("[job]{} Scattering enabled - additional delay: {} ms", job.key, delay)
                    // Adding to the initial delay
                    initialPeriod += delay
                }
            }
            // Actual schedule
            actualSchedule = Schedule(
                    initialPeriod,
                    period,
                    TimeUnit.MILLISECONDS
            )
            // Scheduling now
            scheduledFuture = if (schedule.period > 0) {
                scheduledExecutorService.scheduleWithFixedDelay(
                        this,
                        initialPeriod,
                        period,
                        TimeUnit.MILLISECONDS
                )
            } else {
                logger.debug("[job]{} Job not scheduled since period = 0", job.key)
                null
            }
        }

        override fun run() {
            if (!schedulerPaused.get()) {
                doRun(false)
            }
        }

        fun doRun(force: Boolean): Optional<Future<*>> {
            logger.debug("[job][run]{} Trying to run now - forced = {}", job.key, force)
            if (job.isValid) {
                if (job.isDisabled) {
                    logger.debug("[job][run]{} Not allowed to run now because disabled", job.key)
                    return Optional.empty()
                } else if (paused.get() && !force) {
                    logger.debug("[job][run]{} Not allowed to run now because paused", job.key)
                    return Optional.empty()
                } else if (currentExecution.get() != null) {
                    logger.debug("[job][run]{} Not allowed to run now because already running", job.key)
                    return Optional.empty()
                } else {
                    // Task to run
                    val run = run
                    // Gets the executor for this job
                    val executor = getExecutorService(job)
                    // Scheduling
                    logger.debug("[job][run]{} Job task submitted asynchronously", job.key)
                    val execution = executor.submit(run)
                    currentExecution.set(execution)
                    return Optional.of(execution)
                }
            } else {
                logger.debug("[job][run]{} Not valid - removing from schedule", job.key)
                unschedule(job.key, false)
                return Optional.empty()
            }
        }

        fun stop(): Boolean {
            logger.debug("[job]{} Stopping job", job.key)
            return currentExecution.updateAndGet { current ->
                current?.cancel(true)
                null
            } == null
        }

        fun cancel(forceStop: Boolean): Boolean {
            logger.debug("[job]{} Cancelling job (forcing = {})", job.key, forceStop)
            if (forceStop) {
                stop()
            }
            return scheduledFuture != null && scheduledFuture.cancel(forceStop)
        }

        private fun getNextRunDate(valid: Boolean): LocalDateTime? {
            return if (valid && scheduledFuture != null) {
                Time.now().plus(
                        scheduledFuture.getDelay(TimeUnit.SECONDS),
                        ChronoUnit.SECONDS
                )
            } else {
                null
            }
        }

        fun pause() {
            if (scheduledFuture != null) {
                paused.set(true)
                jobListener.onJobPaused(job.key)
            }
        }

        fun resume() {
            if (scheduledFuture != null) {
                paused.set(false)
                jobListener.onJobResumed(job.key)
            }
        }

        private inner class DefaultJobRunListener : JobRunListener {

            override fun progress(progress: JobRunProgress) {
                jobListener.onJobProgress(job.key, progress)
                logger.debug("[job][progress]{} {}",
                        job.key,
                        progress.text
                )
                runProgress.set(progress)
            }

        }

    }
}
