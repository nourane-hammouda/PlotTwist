package fr.miage.plottwist.investigation.domain.exception;

public class F4DeclarationNotFoundException extends RuntimeException {
    public F4DeclarationNotFoundException(String declarationId) {
        super("F4 declaration not found: " + declarationId);
    }
}
