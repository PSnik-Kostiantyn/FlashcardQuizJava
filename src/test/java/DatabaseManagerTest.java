import org.example.db.DatabaseManager;
import org.example.enteties.Card;
import org.example.enteties.Deck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTest {
    private DatabaseManager db;
    private Connection testConnection;

    /**
     * Before each test, we create an in-memory SQLite database and set it in the DatabaseManager instance.
     * This allows us to test the database operations without affecting a real database file.
     */
    @BeforeEach
    void setUp() throws SQLException, NoSuchFieldException, IllegalAccessException {
        testConnection = DriverManager.getConnection("jdbc:sqlite::memory:");

        db = DatabaseManager.getInstance();

        Field connectionField = DatabaseManager.class.getDeclaredField("connection");
        connectionField.setAccessible(true);
        connectionField.set(db, testConnection);

        createTestTables();

        clearTables();
    }

    private void createTestTables() throws SQLException {
        String decksSql = "CREATE TABLE IF NOT EXISTS decks (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE NOT NULL);";
        String cardsSql = "CREATE TABLE IF NOT EXISTS cards (id INTEGER PRIMARY KEY AUTOINCREMENT, deck_id INTEGER, question TEXT NOT NULL, answer TEXT NOT NULL, FOREIGN KEY(deck_id) REFERENCES decks(id) ON DELETE CASCADE);";
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute(decksSql);
            stmt.execute(cardsSql);
        }
    }

    private void clearTables() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM cards");
            stmt.execute("DELETE FROM decks");
        }
    }

    @Test
    void testCreateDeck() throws SQLException {
        long id = db.createDeck("TestDeck1");
        assertTrue(id > 0);
        List<Deck> decks = db.getAllDecks();
        assertEquals(1, decks.size());
        assertEquals("TestDeck1", decks.getFirst().getName());
    }

    @Test
    void testDeleteDeck() throws SQLException {
        long id = db.createDeck("DeckToDelete1");
        db.deleteDeck(id);
        List<Deck> decks = db.getAllDecks();
        assertTrue(decks.isEmpty());
    }

    @Test
    void testAddCard() throws SQLException {
        long deckId = db.createDeck("CardDeck1");
        db.addCard(deckId, "Q1", "A1");
        List<Card> cards = db.getCardsForDeck(deckId);
        assertEquals(1, cards.size());
        assertEquals("Q1", cards.getFirst().getQuestion());
        assertEquals("A1", cards.getFirst().getAnswer());
    }

    @Test
    void testUpdateCard() throws SQLException {
        long deckId = db.createDeck("UpdateDeck1");
        db.addCard(deckId, "OldQ", "OldA");
        List<Card> cards = db.getCardsForDeck(deckId);
        long cardId = cards.getFirst().getId();
        db.updateCard(cardId, "NewQ", "NewA");
        cards = db.getCardsForDeck(deckId);
        assertEquals("NewQ", cards.getFirst().getQuestion());
        assertEquals("NewA", cards.getFirst().getAnswer());
    }

    @Test
    void testDeleteCard() throws SQLException {
        long deckId = db.createDeck("DeleteCardDeck1");
        db.addCard(deckId, "Q", "A");
        List<Card> cards = db.getCardsForDeck(deckId);
        long cardId = cards.getFirst().getId();
        db.deleteCard(cardId);
        cards = db.getCardsForDeck(deckId);
        assertTrue(cards.isEmpty());
    }

    @Test
    void testCreateDeckWithDuplicateName() {
        long id1 = db.createDeck("DuplicateDeck");
        assertTrue(id1 > 0);
        assertThrows(SQLException.class, () -> db.createDeck("DuplicateDeck"));
    }
}