package fr.miage.plottwist.investigation.domain.algo;

import fr.miage.plottwist.investigation.domain.model.Declaration;
import fr.miage.plottwist.investigation.domain.model.GeneratedF4Scenario;
import fr.miage.plottwist.investigation.domain.model.GeneratedF4Scenario.GeneratedDeclaration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.UUID;

public final class F4InvestigationGenerator {
    public static final String GENERATED_SEED_PREFIX = "#AUTO-";

    private static final List<Person> PEOPLE = List.of(
            new Person("s1", "Éloïse Varenne"),
            new Person("s2", "Gabriel Moreau"),
            new Person("s3", "Léonard Faye"),
            new Person("s4", "Mina Sorel"),
            new Person("s5", "Thomas Rivière"));
    private static final List<Place> PLACES = List.of(
            new Place("r1", "La scène"),
            new Place("r2", "Les coulisses"),
            new Place("r3", "La loge 3"),
            new Place("r4", "La régie"));
    private static final List<String> TITLES = List.of(
            "Le carnet des coulisses",
            "La répétition interrompue",
            "Le dernier rappel",
            "Une nuit en régie");

    public static boolean isGeneratedSeed(String seed) {
        if (seed == null || !seed.startsWith(GENERATED_SEED_PREFIX)) {
            return false;
        }
        try {
            UUID.fromString(seed.substring(GENERATED_SEED_PREFIX.length()));
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public GeneratedF4Scenario generate(String seed) {
        if (!isGeneratedSeed(seed)) {
            throw new IllegalArgumentException("La graine d’enquête générée est invalide.");
        }

        UUID identifier = UUID.fromString(seed.substring(GENERATED_SEED_PREFIX.length()));
        SplittableRandom random = new SplittableRandom(
                identifier.getMostSignificantBits() ^ identifier.getLeastSignificantBits());
        List<Person> people = new ArrayList<>(PEOPLE);
        List<Place> places = new ArrayList<>(PLACES);
        shuffle(people, random);
        shuffle(places, random);

        Person ubiquitySuspect = people.get(0);
        Person solitarySuspect = people.get(1);
        Person witness = people.get(2);
        Person firstWitness = people.get(3);
        Person secondWitness = people.get(4);
        Place firstPlace = places.get(0);
        Place secondPlace = places.get(1);
        Place sharedPlace = places.get(2);
        Place finalPlace = places.get(3);

        int firstOffset = random.nextInt(15);
        int solitaryOffset = random.nextInt(10);
        LocalTime firstStart = LocalTime.of(20, 0).plusMinutes(firstOffset);
        LocalTime solitaryStart = LocalTime.of(20, 45).plusMinutes(solitaryOffset);

        List<GeneratedDeclaration> declarations = List.of(
                generated("d1", ubiquitySuspect, firstPlace, firstStart, firstStart.plusMinutes(25), false),
                generated("d2", ubiquitySuspect, secondPlace, firstStart.plusMinutes(15), firstStart.plusMinutes(35), false),
                generated("d3", solitarySuspect, sharedPlace, solitaryStart, solitaryStart.plusMinutes(20), true),
                generated("d4", witness, sharedPlace, solitaryStart.plusMinutes(5), solitaryStart.plusMinutes(15), false),
                generated("d5", firstWitness, finalPlace, LocalTime.of(21, 20), LocalTime.of(21, 35), false),
                generated("d6", secondWitness, firstPlace, LocalTime.of(21, 40), LocalTime.of(21, 55), false));

        String introduction = "Six témoignages ont été recueillis après la disparition du carnet de régie. "
                + "Reliez les versions incompatibles et identifiez les déclarations mensongères pour faire avancer l’enquête.";
        return new GeneratedF4Scenario(
                seed,
                TITLES.get(random.nextInt(TITLES.size())),
                introduction,
                declarations,
                Set.of("d1", "d3"));
    }

    private static GeneratedDeclaration generated(
            String id,
            Person person,
            Place place,
            LocalTime start,
            LocalTime end,
            boolean claimsAlone) {
        return new GeneratedDeclaration(
                id,
                person.name(),
                place.name(),
                new Declaration(person.id(), place.id(), start, end, claimsAlone));
    }

    private static <T> void shuffle(List<T> values, SplittableRandom random) {
        for (int index = values.size() - 1; index > 0; index--) {
            int otherIndex = random.nextInt(index + 1);
            T value = values.get(index);
            values.set(index, values.get(otherIndex));
            values.set(otherIndex, value);
        }
    }

    private record Person(String id, String name) {}

    private record Place(String id, String name) {}
}
