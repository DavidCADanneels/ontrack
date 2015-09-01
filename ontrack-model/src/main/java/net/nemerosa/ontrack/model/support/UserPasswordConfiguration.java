package net.nemerosa.ontrack.model.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.function.Function;

public interface UserPasswordConfiguration<T extends UserPasswordConfiguration<T>> extends Configuration<T> {

    /**
     * Name of the configuration
     */
    String getName();

    String getUser();

    String getPassword();

    T withPassword(String password);

    T clone(String targetConfigurationName, Function<String, String> replacementFunction);

    @JsonIgnore
    default Optional<UserPassword> getCredentials() {
        String user = getUser();
        if (StringUtils.isNotBlank(user)) {
            return Optional.of(new UserPassword(user, getPassword()));
        } else {
            return Optional.empty();
        }
    }
}
