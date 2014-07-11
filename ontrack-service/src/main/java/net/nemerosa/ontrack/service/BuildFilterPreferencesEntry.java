package net.nemerosa.ontrack.service;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class BuildFilterPreferencesEntry {

    private final String name;
    private final String type;
    private final Map<String, List<String>> data;

}
