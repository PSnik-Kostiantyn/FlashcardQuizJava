package org.example.enteties;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Deck {
    private long id;
    private String name;
    private List<Card> cards = new ArrayList<>();
}
