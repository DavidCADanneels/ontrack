package net.nemerosa.ontrack.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Default encryption service
 */
@Component
public class EncryptionServiceImpl implements EncryptionService {

    private final ConfidentialKey key;

    public EncryptionServiceImpl(ConfidentialKey key) {
        this.key = key;
    }

    @Autowired
    public EncryptionServiceImpl(ConfidentialStore confidentialStore) {
        this(new CryptoConfidentialKey(confidentialStore, EncryptionServiceImpl.class, "encryption"));
    }

    @Override
    public String encrypt(String plain) {
        return plain != null ? key.encrypt(plain) : null;
    }

    @Override
    public String decrypt(String crypted) {
        return crypted != null ? key.decrypt(crypted) : null;
    }
}
