package net.nemerosa.ontrack.extension.git.model;

import lombok.Data;
import net.nemerosa.ontrack.extension.git.client.GitCommit;

@Data
public class GitUICommit {

    private final GitCommit commit;
    private final String annotatedMessage;
    private final String fullAnnotatedMessage;
    private final String link;

}
