package ru.yandex.practicum;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WordleDictionary {

    public static final int WORD_LENGTH = 5;

    private final List<String> words;
    private final Random random = new Random();

    public WordleDictionary(List<String> words) {
        this.words = new ArrayList<>(words);
    }

    public List<String> getWords() {
        return new ArrayList<>(words);
    }

    public boolean contains(String word) {
        return words.contains(word);
    }

    public String getRandomWord() throws EmptyDictionaryException {
        if (words.isEmpty()) {
            throw new EmptyDictionaryException("Словарь пуст, невозможно выбрать слово");
        }

        return words.get(random.nextInt(words.size()));
    }

    public int size() {
        return words.size();
    }

    public static String analyzeWord(String guess, String answer) {
        if (guess == null || answer == null
                || guess.length() != WORD_LENGTH || answer.length() != WORD_LENGTH) {
            return null;
        }

        char[] result = new char[WORD_LENGTH];
        boolean[] answerUsed = new boolean[WORD_LENGTH];
        boolean[] guessUsed = new boolean[WORD_LENGTH];

        for (int i = 0; i < WORD_LENGTH; i++) {
            if (guess.charAt(i) == answer.charAt(i)) {
                result[i] = '+';
                answerUsed[i] = true;
                guessUsed[i] = true;
            }
        }

        for (int i = 0; i < WORD_LENGTH; i++) {
            if (guessUsed[i]) {
                continue;
            }

            char guessChar = guess.charAt(i);
            boolean found = false;

            for (int j = 0; j < WORD_LENGTH; j++) {
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
