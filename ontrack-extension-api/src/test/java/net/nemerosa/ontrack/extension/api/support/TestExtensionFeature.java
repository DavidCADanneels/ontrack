package net.nemerosa.ontrack.extension.api.support;

import net.nemerosa.ontrack.model.extension.ExtensionFeature;
import org.springframework.stereotype.Component;

@Component
public class TestExtensionFeature implements ExtensionFeature {
    @Override
    public String getId() {
        return "test";
    }

    @Override
    public String getName() {
        return "Test extension";
    }

    @Override
    public String getDescription() {
        return "Extensions for tests";
    }

    @Override
    public String getVersion() {
        return "test";
    }
}
