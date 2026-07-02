package ru.yandex.practicum;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WordleDictionary {

    private final List<String> words;

    public WordleDictionary(List<String> words) {
        this.words = new ArrayList<>(words);
    }

    public List<String> getWords() {
        return new ArrayList<>(words);
    }

    public boolean contains(String word) {
        return words.contains(word);
    }

    public String getRandomWord() {
        if (words.isEmpty()) {
            return null;
        }
        Random random = new Random();
        return words.get(random.nextInt(words.size()));
    }

    public int size() {
        return words.size();
    }

    public static String analyzeWord(String guess, String answer) {
        if (guess == null || answer == null || guess.length() != 5 || answer.length() != 5) {
            return null;
        }

        char[] result = new char[5];
        boolean[] answerUsed = new boolean[5];
        boolean[] guessUsed = new boolean[5];

        for (int i = 0; i < 5; i++) {
            if (guess.charAt(i) == answer.charAt(i)) {
                result[i] = '+';
                answerUsed[i] = true;
                guessUsed[i] = true;
            }
        }

        for (int i = 0; i < 5; i++) {
            if (guessUsed[i]) {
                continue;
            }

            char guessChar = guess.charAt(i);
            boolean found = false;

            for (int j = 0; j < 5; j++) {
                if (!answerUsed[j] && guessChar == answer.charAt(j)) {
                    result[i] = '^';
                    answerUsed[j] = true;
                    found = true;
                    break;
                }
            }

            if (!found) {
                result[i] = '-';
            }
        }
        return new String(result);
    }
}