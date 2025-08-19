package org.example;

import org.example.db.DatabaseManager;
import org.example.enteties.Card;
import org.example.enteties.Deck;
import org.example.json.JsonManager;

import java.util.List;
import java.util.Scanner;

/**
 * Main entry point for the Flashcard Quiz application.
 * Provides console-based menu for managing decks, cards, studying, and JSON import/export.
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final DatabaseManager db = DatabaseManager.getInstance();

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            System.out.println("\nMain Menu:");
            System.out.println("1. Start Study");
            System.out.println("2. Manage Decks");
            System.out.println("3. Export to JSON");
            System.out.println("4. Import from JSON");
            System.out.println("5. Exit");
            int choice = getIntInput("Enter choice: ");
            switch (choice) {
                case 1 -> startStudy();
                case 2 -> manageDecks();
                case 3 -> exportToJson();
                case 4 -> importFromJson();
                case 5 -> running = false;
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
        scanner.close();
    }

    private static int getIntInput(String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Invalid number. " + prompt);
            }
        }
    }

    private static String getNonEmptyInput(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        while (input.isBlank()) {
            System.out.print("Input cannot be empty. " + prompt);
            input = scanner.nextLine().trim();
        }
        return input;
    }

    private static void manageDecks() {
        boolean managing = true;
        while (managing) {
            System.out.println("\nDeck Management:");
            System.out.println("1. Create Deck");
            System.out.println("2. List Decks");
            System.out.println("3. Select Deck for Card Management");
            System.out.println("4. Delete Deck");
            System.out.println("5. Back");
            int choice = getIntInput("Enter choice: ");
            switch (choice) {
                case 1 -> {
                    String name = getNonEmptyInput("Enter deck name: ");
                    try {
                        db.createDeck(name);
                        System.out.println("Deck created successfully.");
                    } catch (Exception e) {
                        System.out.println("Error creating deck: " + e.getMessage());
                    }
                }
                case 2 -> listDecks();
                case 3 -> {
                    Deck deck = selectDeck();
                    if (deck != null) {
                        manageCards(deck);
                    } else {
                        System.out.println("Deck not found.");
                    }
                }
                case 4 -> {
                    Deck deck = selectDeck();
                    if (deck != null) {
                        db.deleteDeck(deck.getId());
                        System.out.println("Deck deleted successfully.");
                    } else {
                        System.out.println("Deck not found.");
                    }
                }
                case 5 -> managing = false;
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void listDecks() {
        List<Deck> decks = db.getAllDecks();
        if (decks.isEmpty()) {
            System.out.println("No decks available.");
        } else {
            System.out.println("Available Decks:");
            decks.forEach(deck -> System.out.println("ID: " + deck.getId() + ", Name: " + deck.getName()));
        }
    }

    private static Deck selectDeck() {
        listDecks();
        long id = getIntInput("Enter deck ID: ");
        return db.getDeckById(id);
    }

    private static void manageCards(Deck deck) {
        boolean managing = true;
        while (managing) {
            System.out.println("\nCard Management for Deck: " + deck.getName());
            System.out.println("1. Add Card");
            System.out.println("2. List Cards");
            System.out.println("3. Edit Card");
            System.out.println("4. Delete Card");
            System.out.println("5. Back");
            int choice = getIntInput("Enter choice: ");
            switch (choice) {
                case 1 -> {
                    String question = getNonEmptyInput("Enter question: ");
                    String answer = getNonEmptyInput("Enter answer: ");
                    try {
                        db.addCard(deck.getId(), question, answer);
                        System.out.println("Card added successfully.");
                    } catch (Exception e) {
                        System.out.println("Error adding card: " + e.getMessage());
                    }
                }
                case 2 -> listCards(deck);
                case 3 -> editCard(deck);
                case 4 -> deleteCard(deck);
                case 5 -> managing = false;
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void listCards(Deck deck) {
        List<Card> cards = db.getCardsForDeck(deck.getId());
        if (cards.isEmpty()) {
            System.out.println("No cards in this deck.");
        } else {
            System.out.println("Cards in Deck:");
            cards.forEach(card -> System.out.println("ID: " + card.getId() + ", Question: " + card.getQuestion() + ", Answer: " + card.getAnswer()));
        }
    }

    private static void editCard(Deck deck) {
        listCards(deck);
        long cardId = getIntInput("Enter card ID to edit: ");
        String newQuestion = getNonEmptyInput("Enter new question: ");
        String newAnswer = getNonEmptyInput("Enter new answer: ");
        try {
            db.updateCard(cardId, newQuestion, newAnswer);
            System.out.println("Card updated successfully.");
        } catch (Exception e) {
            System.out.println("Error updating card: " + e.getMessage());
        }
    }

    private static void deleteCard(Deck deck) {
        listCards(deck);
        long cardId = getIntInput("Enter card ID to delete: ");
        try {
            db.deleteCard(cardId);
            System.out.println("Card deleted successfully.");
        } catch (Exception e) {
            System.out.println("Error deleting card: " + e.getMessage());
        }
    }

    private static void startStudy() {
        Deck deck = selectDeck();
        if (deck == null) {
            System.out.println("Deck not found.");
            return;
        }
        List<Card> cards = deck.getCards();
        if (cards.isEmpty()) {
            System.out.println("No cards in this deck to study.");
            return;
        }
        System.out.println("\nStudy Mode for Deck: " + deck.getName());
        for (Card card : cards) {
            System.out.println("Question: " + card.getQuestion());
            String userAnswer = getNonEmptyInput("Your answer is: ");
            if (userAnswer.equalsIgnoreCase(card.getAnswer())) {
                System.out.println("Correct!");
            } else {
                System.out.println("Incorrect. Correct answer: " + card.getAnswer());
            }
            System.out.println();
        }
        System.out.println("Study session completed.");
    }

    private static void exportToJson() {
        String filePath = getNonEmptyInput("Enter file path for export (for example: decks.json): ");
        List<Deck> decks = db.getAllDecks();
        JsonManager.saveDecksToJson(decks, filePath);
        System.out.println("Export completed.");
    }

    private static void importFromJson() {
        String filePath = getNonEmptyInput("Enter file path for import (for example: decks.json): ");
        List<Deck> importedDecks = JsonManager.loadDecksFromJson(filePath);
        if (importedDecks == null) {
            System.out.println("Import failed.");
            return;
        }
        for (Deck importedDeck : importedDecks) {
            Deck existing = db.getDeckByName(importedDeck.getName());
            long deckId;
            try {
                if (existing == null) {
                    deckId = db.createDeck(importedDeck.getName());
                } else {
                    deckId = existing.getId();
                }
                for (Card card : importedDeck.getCards()) {
                    db.addCard(deckId, card.getQuestion(), card.getAnswer());
                }
            } catch (Exception e) {
                System.out.println("Error importing deck " + importedDeck.getName() + ": " + e.getMessage());
            }
        }
        System.out.println("Import completed.");
    }
}