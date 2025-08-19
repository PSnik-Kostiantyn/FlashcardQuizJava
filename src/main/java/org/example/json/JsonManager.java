package org.example.json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.enteties.Deck;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

/**
 * Utility class for handling JSON export with Gson and import of decks.
 */
public class JsonManager {
    private static final Gson gson = new Gson();

    public static void saveDecksToJson(List<Deck> decks, String filePath) {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(decks, writer);
        } catch (IOException e) {
            System.err.println("Error saving to JSON: " + e.getMessage());
        }
    }

    public static List<Deck> loadDecksFromJson(String filePath) {
        try (Reader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, new TypeToken<List<Deck>>() {}.getType());
        } catch (IOException e) {
            System.err.println("Error loading from JSON: " + e.getMessage());
            return null;
        }
    }
}