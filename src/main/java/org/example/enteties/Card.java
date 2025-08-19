package org.example.enteties;

import lombok.Data;

@Data
public class Card {
    private long id;
    private String question;
    private String answer;
}