import org.example.enteties.Card;
import org.example.enteties.Deck;
import org.example.json.JsonManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonManagerTest {

    /**
     * Tests saving and loading decks to/from JSON.
     * Uses a temporary directory to avoid file system dependencies.
     */
    @Test
    void testSaveAndLoadDecks(@TempDir Path tempDir) {
        File tempFile = tempDir.resolve("test.json").toFile();

        Deck deck = new Deck();
        deck.setId(1);
        deck.setName("TestDeck");
        Card card = new Card();
        card.setId(1);
        card.setQuestion("Q");
        card.setAnswer("A");
        deck.getCards().add(card);
        List<Deck> decks = new ArrayList<>();
        decks.add(deck);

        JsonManager.saveDecksToJson(decks, tempFile.getAbsolutePath());

        List<Deck> loadedDecks = JsonManager.loadDecksFromJson(tempFile.getAbsolutePath());
        assertNotNull(loadedDecks);
        assertEquals(1, loadedDecks.size());
        assertEquals("TestDeck", loadedDecks.getFirst().getName());
        assertEquals(1, loadedDecks.getFirst().getCards().size());
        assertEquals("Q", loadedDecks.getFirst().getCards().getFirst().getQuestion());
        assertEquals("A", loadedDecks.getFirst().getCards().getFirst().getAnswer());
    }

    @Test
    void testLoadNonExistentFile() {
        List<Deck> loaded = JsonManager.loadDecksFromJson("nonexistent.json");
        assertNull(loaded);
    }
}