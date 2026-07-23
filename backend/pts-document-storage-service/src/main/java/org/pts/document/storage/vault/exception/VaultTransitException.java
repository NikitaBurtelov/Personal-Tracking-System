package org.pts.document.storage.vault.exception;

public class VaultTransitException extends RuntimeException {
    public VaultTransitException(String message) {
        super(message);
    }

    public VaultTransitException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
