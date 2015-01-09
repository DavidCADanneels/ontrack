package net.nemerosa.ontrack.extension.svn.db;

import net.nemerosa.ontrack.repository.support.AbstractJdbcRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

@Repository
public class SVNIssueRevisionJdbcDao extends AbstractJdbcRepository implements SVNIssueRevisionDao {

    private static final int ISSUE_KEY_MAX_LENGTH = 20;
    private final Logger logger = LoggerFactory.getLogger(SVNIssueRevisionDao.class);

    @Autowired
    public SVNIssueRevisionJdbcDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void link(int repositoryId, long revision, String key) {
        if (StringUtils.isBlank(key)) {
            logger.warn("Cannot insert a null or blank key (revision {})", revision);
        } else if (key.length() > ISSUE_KEY_MAX_LENGTH) {
            logger.warn("Cannot insert a key longer than {} characters: {} for revision {}", ISSUE_KEY_MAX_LENGTH, key, revision);
        } else {
            getNamedParameterJdbcTemplate().update(
                    "INSERT INTO EXT_SVN_REVISION_ISSUE (REPOSITORY, REVISION, ISSUE) VALUES (:repository, :revision, :key)",
                    params("revision", revision).addValue("key", key).addValue("repository", repositoryId));
        }
    }

    @Override
    public List<String> findIssuesByRevision(int repositoryId, long revision) {
        return getNamedParameterJdbcTemplate().queryForList(
                "SELECT ISSUE FROM EXT_SVN_REVISION_ISSUE WHERE REPOSITORY = :repository AND REVISION = :revision ORDER BY ISSUE",
                params("revision", revision).addValue("repository", repositoryId),
                String.class
        );
    }

    @Override
    public Optional<String> findIssueByKey(int repositoryId, String issueKey) {
        return Optional.ofNullable(
                getFirstItem(
                        "SELECT ISSUE FROM EXT_SVN_REVISION_ISSUE WHERE REPOSITORY = :repository AND ISSUE = :issue ORDER BY REVISION LIMIT 1",
                        params("repository", repositoryId).addValue("issue", issueKey),
                        String.class
                )
        );
    }

    @Override
    public List<Long> findRevisionsByIssue(int repositoryId, String issueKey) {
        return getNamedParameterJdbcTemplate().queryForList(
                "SELECT REVISION FROM EXT_SVN_REVISION_ISSUE WHERE REPOSITORY = :repository AND ISSUE = :key ORDER BY REVISION DESC",
                params("key", issueKey).addValue("repository", repositoryId),
                Long.class);
    }

    @Override
    public OptionalLong findLastRevisionByIssue(int repositoryId, String issueKey) {
        Long revision = getFirstItem(
                "SELECT REVISION FROM EXT_SVN_REVISION_ISSUE WHERE REPOSITORY = :repository AND ISSUE = :key ORDER BY REVISION DESC LIMIT 1",
                params("key", issueKey).addValue("repository", repositoryId),
                Long.class);
        return optionalLong(revision);
    }

    private OptionalLong optionalLong(Long revision) {
        if (revision != null) {
            return OptionalLong.of(revision);
        } else {
            return OptionalLong.empty();
        }
    }

    @Override
    public OptionalLong findLastRevisionByIssuesAndBranch(int repositoryId, Collection<String> issueKeys, String branch) {
        Long revision = getFirstItem(
                "SELECT RI.REVISION FROM EXT_SVN_REVISION_ISSUE RI " +
                        "INNER JOIN EXT_SVN_REVISION R ON R.REPOSITORY = RI.REPOSITORY AND R.REVISION = RI.REVISION " +
                        "WHERE RI.ISSUE IN (:keys) " +
                        "AND R.BRANCH = :branch " +
                        "ORDER BY RI.REVISION DESC " +
                        "LIMIT 1",
                params("keys", issueKeys).addValue("repository", repositoryId).addValue("branch", branch),
                Long.class
        );
        return optionalLong(revision);
    }

}
