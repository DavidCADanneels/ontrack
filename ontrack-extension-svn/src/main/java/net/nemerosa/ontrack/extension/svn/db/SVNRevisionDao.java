package net.nemerosa.ontrack.extension.svn.db;

import java.time.LocalDateTime;
import java.util.List;

public interface SVNRevisionDao {

    long getLast(int repositoryId);

    void addRevision(int repositoryId, long revision, String author, LocalDateTime dateTime, String message, String branch);

    void addMergedRevisions(int repositoryId, long revision, List<Long> mergedRevisions);

    TRevision getLastRevision(int repositoryId);
}
