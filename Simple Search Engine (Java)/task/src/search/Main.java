package search;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static List<String> dataLines = new ArrayList<>();
    private static final Map<String, List<Integer>> invertedIndex = new HashMap<>();

    public static void main(String[] args) {
        if (args.length != 2 || !args[0].equals("--data")) {
            System.out.println("Usage: java Main --data <filename>");
            return;
        }

        String filename = args[1];
        readDataFromFile(filename);
        buildInvertedIndex();
        displayMenu();
    }

    private static void readDataFromFile(String filename) {
        try (Stream<String> lines = Files.lines(Path.of(filename))) {
            dataLines = lines.map(String::trim).toList();
        } catch (IOException e) {
            System.out.println("File not found: " + filename);
        }
    }

    private static void buildInvertedIndex() {
        for (int i = 0; i < dataLines.size(); i++) {
            String[] words = dataLines.get(i).toLowerCase().split("\\s+");
            for (String word : words) {
                invertedIndex.computeIfAbsent(word, k -> new ArrayList<>()).add(i);
            }
        }
    }

    private static void displayMenu() {
        int choice;
        do {
            System.out.println("\n=== Menu ===");
            System.out.println("1. Find a person");
            System.out.println("2. Print all people");
            System.out.println("0. Exit");
            System.out.print("> ");
            choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1 -> findPerson();
                case 2 -> printAllPeople();
                case 0 -> System.out.println("Bye!");
                default -> System.out.println("Incorrect option! Try again.");
            }
        } while (choice != 0);

        scanner.close();
    }

    private static void findPerson() {
        System.out.println("Select a matching strategy: ALL, ANY, NONE");
        String strategy = scanner.nextLine().trim().toUpperCase();

        System.out.println("Enter a name or email to search all suitable people:");
        String query = scanner.nextLine().trim().toLowerCase();

        Set<Integer> resultIndices = switch (strategy) {
            case "ALL" -> findAll(query.split("\\s+"));
            case "ANY" -> findAny(query.split("\\s+"));
            case "NONE" -> findNone(query.split("\\s+"));
            default -> {
                System.out.println("Unknown strategy. Try again.");
                yield Collections.emptySet();
            }
        };

        if (resultIndices.isEmpty()) {
            System.out.println("No matching people found.");
        } else {
            System.out.println(resultIndices.size() + " persons found:");
            resultIndices.forEach(index -> System.out.println(dataLines.get(index)));
        }
    }

    private static Set<Integer> findAll(String[] words) {
        return Arrays.stream(words)
                .map(word -> new HashSet<>(invertedIndex.getOrDefault(word, List.of())))
                .map(set -> (Set<Integer>) set)  // Explicitly cast to Set<Integer>
                .reduce((set1, set2) -> {
                    set1.retainAll(set2);
                    return set1;
                }).orElse(Collections.emptySet());
    }

    private static Set<Integer> findAny(String[] words) {
        return Arrays.stream(words)
                .flatMap(word -> invertedIndex.getOrDefault(word, List.of()).stream())
                .collect(Collectors.toSet());
    }

    private static Set<Integer> findNone(String[] words) {
        Set<Integer> allIndices = invertedIndex.values()
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());
        Set<Integer> excludedIndices = Arrays.stream(words)
                .flatMap(word -> invertedIndex.getOrDefault(word, List.of()).stream())
                .collect(Collectors.toSet());

        allIndices.removeAll(excludedIndices);
        return allIndices;
    }

    private static void printAllPeople() {
        System.out.println("\n=== List of people ===");
        dataLines.forEach(System.out::println);
    }
}