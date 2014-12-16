package net.nemerosa.ontrack.extension.git.model;

import lombok.Data;
import net.nemerosa.ontrack.git.model.plot.GPlot;

import java.util.List;

@Data
public class GitUILog {

    private final GPlot plot;
    private final List<GitUICommit> commits;

}
