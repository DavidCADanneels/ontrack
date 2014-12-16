package net.nemerosa.ontrack.extension.git.model;

import com.fasterxml.jackson.databind.JsonNode;
import net.nemerosa.ontrack.git.GitRepositoryClient;
import net.nemerosa.ontrack.model.form.Form;
import net.nemerosa.ontrack.model.structure.Build;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Defines the way to link builds to Git commits, in order to manage the change logs, the Git searches
 * and synchronisations.
 *
 * @param <T> Type of configuration data
 */
public interface BuildGitCommitLink<T> {

    /**
     * ID of the link
     */
    String getId();

    /**
     * Display name for the link
     */
    String getName();

    /**
     * Clones the configuration.
     */
    T clone(T data, Function<String, String> replacementFunction);

    /**
     * For the given {@code build}, returns the corresponding Git commit
     *
     * @param build Build to get the commit for
     * @param data  Configuration of the link
     * @return Committish (short or long SHA, tag, head, etc.)
     */
    String getCommitFromBuild(Build build, T data);

    /**
     * Parses the configuration from a JSON node
     */
    T parseData(JsonNode node);

    /**
     * Formats the configuration data as JSON
     */
    JsonNode toJson(T data);

    /**
     * Creates a form for the edition of the link configuration.
     */
    Form getForm();

    /**
     * Gets the list of build names from Git reference candidates
     *
     * @param commit              The commit to start from
     * @param gitClient           The Git client to use for the connection
     * @param branchConfiguration Git branch configuration
     * @param data                Configuration data
     * @return Candidate build names
     */
    Stream<String> getBuildCandidateReferences(String commit, GitRepositoryClient gitClient, GitBranchConfiguration branchConfiguration, T data);

    /**
     * Checks if a build is eligible after it has been loaded from a
     * {@linkplain #getBuildCandidateReferences(String, net.nemerosa.ontrack.git.GitRepositoryClient, GitBranchConfiguration, Object)}
     *
     * @param build Build to check
     * @param data  Configuration data
     * @return <code>true</code> if the build is linked to the configuration
     */
    boolean isBuildEligible(Build build, T data);

}
