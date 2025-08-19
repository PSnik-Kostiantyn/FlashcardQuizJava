package org.example.db;

import lombok.SneakyThrows;
import org.example.enteties.Card;
import org.example.enteties.Deck;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Singleton class for managing SQLite database operations.
 * Handles CRUD for decks and cards.
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        init();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    @SneakyThrows
    private void init() {
        connection = DriverManager.getConnection("jdbc:sqlite:flashcards.db");
        createTables();
    }

    @SneakyThrows
    private void createTables() {
        String decksSql = "CREATE TABLE IF NOT EXISTS decks (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE NOT NULL);";
        String cardsSql = "CREATE TABLE IF NOT EXISTS cards (id INTEGER PRIMARY KEY AUTOINCREMENT, deck_id INTEGER, question TEXT NOT NULL, answer TEXT NOT NULL, FOREIGN KEY(deck_id) REFERENCES decks(id) ON DELETE CASCADE);";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(decksSql);
            stmt.execute(cardsSql);
        }
    }

    @SneakyThrows
    public long createDeck(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Deck name cannot be empty");
        }
        PreparedStatement ps = connection.prepareStatement("INSERT INTO decks (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, name);
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            return rs.getLong(1);
        }
        throw new SQLException("Creating deck failed, no ID obtained.");
    }

    @SneakyThrows
    public List<Deck> getAllDecks() {
        List<Deck> decks = new ArrayList<>();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM decks");
        while (rs.next()) {
            Deck deck = new Deck();
            deck.setId(rs.getLong("id"));
            deck.setName(rs.getString("name"));
            deck.setCards(getCardsForDeck(deck.getId()));
            decks.add(deck);
        }
        return decks;
    }

    @SneakyThrows
    public Deck getDeckByName(String name) {
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM decks WHERE name = ?");
        ps.setString(1, name);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Deck deck = new Deck();
            deck.setId(rs.getLong("id"));
            deck.setName(name);
            deck.setCards(getCardsForDeck(deck.getId()));
            return deck;
        }
        return null;
    }

    @SneakyThrows
    public Deck getDeckById(long id) {
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM decks WHERE id = ?");
        ps.setLong(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Deck deck = new Deck();
            deck.setId(id);
            deck.setName(rs.getString("name"));
            deck.setCards(getCardsForDeck(id));
            return deck;
        }
        return null;
    }

    @SneakyThrows
    public void deleteDeck(long id) {
        // Cards are deleted cascade due to foreign key constraint
        PreparedStatement ps = connection.prepareStatement("DELETE FROM decks WHERE id = ?");
        ps.setLong(1, id);
        ps.executeUpdate();
    }

    @SneakyThrows
    public void addCard(long deckId, String question, String answer) {
        if (question == null || question.isBlank() || answer == null || answer.isBlank()) {
            throw new IllegalArgumentException("Question and answer cannot be empty");
        }
        PreparedStatement ps = connection.prepareStatement("INSERT INTO cards (deck_id, question, answer) VALUES (?, ?, ?)");
        ps.setLong(1, deckId);
        ps.setString(2, question);
        ps.setString(3, answer);
        ps.executeUpdate();
    }

    @SneakyThrows
    public List<Card> getCardsForDeck(long deckId) {
        List<Card> cards = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM cards WHERE deck_id = ?");
        ps.setLong(1, deckId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Card card = new Card();
            card.setId(rs.getLong("id"));
            card.setQuestion(rs.getString("question"));
            card.setAnswer(rs.getString("answer"));
            cards.add(card);
        }
        return cards;
    }

    @SneakyThrows
    public void updateCard(long cardId, String question, String answer) {
        if (question == null || question.isBlank() || answer == null || answer.isBlank()) {
            throw new IllegalArgumentException("Question and answer cannot be empty");
        }
        PreparedStatement ps = connection.prepareStatement("UPDATE cards SET question = ?, answer = ? WHERE id = ?");
        ps.setString(1, question);
        ps.setString(2, answer);
        ps.setLong(3, cardId);
        ps.executeUpdate();
    }

    @SneakyThrows
    public void deleteCard(long cardId) {
        PreparedStatement ps = connection.prepareStatement("DELETE FROM cards WHERE id = ?");
        ps.setLong(1, cardId);
        ps.executeUpdate();
    }
}