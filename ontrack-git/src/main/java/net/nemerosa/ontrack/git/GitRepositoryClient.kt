package net.nemerosa.ontrack.git

import net.nemerosa.ontrack.git.model.*
import org.eclipse.jgit.revwalk.RevCommit
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.stream.Stream

/**
 * Defines a client for a Git repository.
 */
interface GitRepositoryClient {

    /**
     * Gets the list of remote branches, as defined under `ref/heads`.
     */
    val remoteBranches: List<String>

    /**
     * List of all tags
     */
    val tags: Collection<GitTag>

    /**
     * Gets the synchronisation status
     */
    val synchronisationStatus: GitSynchronisationStatus

    /**
     * Gets the list of all local branches, and their last commit. If the repository is not synched, or is currently
     * being synched, the map is returned empty.
     */
    val branches: GitBranchesInfo

    /**
     * Tests the connection
     *
     * @throws net.nemerosa.ontrack.common.BaseException When the remote Git repository is not reachable
     */
    fun test()

    /**
     * Makes sure the repository is synchronised with its remote location.
     *
     * @param logger Used to log messages during the synchronisation
     */
    fun sync(logger: Consumer<String>)

    /**
     * Checks if the given repository is compatible with this client. The remote, user name
     * and password must be checked.
     */
    fun isCompatible(repository: GitRepository): Boolean


    /**
     * Gets a Git log between two boundaries.
     *
     * @param from Commitish string
     * @param to   Commitish string
     * @return Stream of commits
     */
    fun log(from: String, to: String): Stream<GitCommit>

    /**
     * Gets a graph Git log between two boundaries.
     *
     * @param from Commitish string
     * @param to   Commitish string
     * @return Git log
     */
    fun graph(from: String, to: String): GitLog

    /**
     * Gets the full hash for a commit
     */
    fun getId(revCommit: RevCommit): String

    /**
     * Gets the abbreviated hash for a commit
     */
    fun getShortId(revCommit: RevCommit): String

    /**
     * Consolidation for a commit
     */
    fun toCommit(revCommit: RevCommit): GitCommit


    /**
     * Scans the whole history.
     *
     * @param branch       Branch to follow
     * @param scanFunction Function that scans the commits. Returns `true` if the scan
     * must not go on, `false` otherwise.
     * @return `true` if at least one call to `scanFunction` has returned `true`.
     */
    fun scanCommits(branch: String, scanFunction: Predicate<RevCommit>): Boolean

    /**
     * Gets the reference string for a branch given with its local name.
     */
    fun getBranchRef(branch: String): String

    /**
     * Gets the earliest commit that contains the commit.
     *
     *
     * Uses the `git tag --contains` command to get all tags that contains the given
     * `gitCommitId`.
     *
     *
     * **Note**: returned tags are *not* ordered.
     */
    fun getTagsWhichContainCommit(gitCommitId: String): Collection<String>

    /**
     * Difference between two commit-ish boundaries
     */
    fun diff(from: String, to: String): GitDiff

    /**
     * Looks for a commit using its hash
     *
     * @param id Commit
     * @return Associated commit or `null` if not found.
     */
    fun getCommitFor(id: String): GitCommit?

    /**
     * Checks if the `commitish` string can be parsed into this repository
     *
     * @param commitish Commitish string
     * @return `true` if this is a valid commit-like entry, `false` otherwise
     */
    fun isCommit(commitish: String): Boolean

    /**
     * Gets the unified diff between two boundaries, for a given list of paths
     *
     * @param from       Commitish from
     * @param to         Commitish to
     * @param pathFilter Filter on the path
     * @return Unified diff
     */
    fun unifiedDiff(from: String, to: String, pathFilter: Predicate<String>): String

    /**
     * Downloads a document
     *
     * @param branch Branch to use when looking for the file
     * @param path Path to the document (relative to the root of the repository)
     * @return Content of the document (assumed to be a [String]) or `null` if not present
     */
    fun download(branch: String, path: String): String?

    /**
     * Resets the repository. Performs even if there is a synchronisation going on.
     */
    fun reset()

    /**
     * Checks the log history and returns `true` if the token can be found.
     *
     * @param token Expression to be searched for
     * @return Result of the search
     */
    fun isPatternFound(token: String): Boolean

    /**
     * Checks the log history and returns the first commit whose message matches with the regex.
     *
     * @param branch Name of the branch to look for the commit on
     * @param regex  Expression to be searched for
     * @return Result of the search (null if not found)
     */
    fun findCommitForRegex(branch: String, regex: String): RevCommit?
}
