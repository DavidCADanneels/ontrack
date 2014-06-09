package net.nemerosa.ontrack.extension.jenkins.client;

public interface JenkinsClient {

    JenkinsJob getJob(String job, boolean details);

    boolean hasSameConnection(JenkinsConnection connection);
}
