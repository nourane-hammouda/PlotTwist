package fr.miage.plottwist.investigation.application.port.out;

import java.util.Set;

public interface F4GroundTruthRepository {
    Set<String> findLyingDeclarationIdsBySeed(String seed);
}
