package net.nemerosa.ontrack.job;

public interface Job {

    /**
     * Key of the job
     */
    JobKey getKey();

    /**
     * Task to be run by the job
     */
    Runnable getTask();

    /**
     * Gets a description for the job
     */
    String getDescription();
}
