package net.nemerosa.ontrack.git;

import net.nemerosa.ontrack.git.model.GitCommit;
import net.nemerosa.ontrack.git.model.GitDiff;
import net.nemerosa.ontrack.git.model.GitLog;
import net.nemerosa.ontrack.git.model.GitTag;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Defines a client for a Git repository.
 */
public interface GitRepositoryClient {

    /**
     * Makes sure the repository is synchronised with its remote location.
     *
     * @param logger Used to log messages during the synchronisation
     */
    void sync(Consumer<String> logger);

    /**
     * Checks if the given repository is compatible with this client. The remote, user name
     * and password must be checked.
     */
    boolean isCompatible(GitRepository repository);


    /**
     * Gets a Git log between two boundaries.
     *
     * @param from Commitish string
     * @param to   Commitish string
     * @return Stream of commits
     */
    Stream<GitCommit> log(String from, String to);

    /**
     * Gets a graph Git log between two boundaries.
     *
     * @param from Commitish string
     * @param to   Commitish string
     * @return Git log
     */
    GitLog graph(String from, String to);

    /**
     * Gets the full hash for a commit
     */
    String getId(RevCommit revCommit);

    /**
     * Gets the abbreviated hash for a commit
     */
    String getShortId(RevCommit revCommit);

    /**
     * Consolidation for a commit
     */
    GitCommit toCommit(RevCommit revCommit);


    /**
     * Scans the whole history.
     *
     * @param branch       Branch to follow
     * @param scanFunction Function that scans the commits. Returns <code>true</code> if the scan
     *                     must not go on, <code>false</code> otherwise.
     * @return <code>true</code> if at least one call to <code>scanFunction</code> has returned <code>true</code>.
     */
    boolean scanCommits(String branch, Predicate<RevCommit> scanFunction);

    /**
     * Gets the reference string for a branch given with its local name.
     */
    String getBranchRef(String branch);

    /**
     * Gets the earliest commit that contains the commit.
     * <p>
     * Uses the <code>git tag --contains</code> command to get all tags that contains the given
     * {@code gitCommitId}.
     * <p>
     * <b>Note</b>: returned tags are <i>not</i> ordered.
     */
    Collection<String> getTagsWhichContainCommit(String gitCommitId);

    /**
     * Gets the list of remote branches, as defined under <code>ref/heads</code>.
     */
    List<String> getRemoteBranches();

    /**
     * Difference between two commit-ish boundaries
     */
    GitDiff diff(String from, String to);

    /**
     * Looks for a commit using its hash
     */
    Optional<GitCommit> getCommitFor(String id);

    /**
     * List of all tags
     */
    Collection<GitTag> getTags();
}