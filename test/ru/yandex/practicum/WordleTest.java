package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WordleTest {

    private static WordleDictionary dictionary;
    private WordleGame game;

    @BeforeAll
    static void setUpAll() {
        List<String> testWords = Arrays.asList(
                "абзац", "автор", "актер", "акция", "алмаз",
                "ангел", "апрель", "атом", "афиша", "базар",
                "балет", "барон", "бегун", "бедро", "берег",
                "биржа", "бланк", "богач"
        );
        dictionary = new WordleDictionary(testWords);
    }

    @BeforeEach
    void setUp() throws EmptyDictionaryException {
        game = new WordleGame(dictionary);
    }

    @Test
    void testDictionaryLoaderNormalization() throws DictionaryLoadException {
        WordleDictionaryLoader loader = new WordleDictionaryLoader();
        WordleDictionary dict = loader.loadDictionary("words_ru.txt");
        assertNotNull(dict);
        assertTrue(dict.size() > 0);

        List<String> words = dict.getWords();
        for (String word : words) {
            assertEquals(5, word.length());
            assertTrue(word.matches("[а-я]+"));
            assertEquals(word.toLowerCase(), word);
        }
    }

    @Test
    void testDictionaryContains() {
        assertTrue(dictionary.contains("абзац"));
        assertTrue(dictionary.contains("автор"));
        assertFalse(dictionary.contains("привет"));
        assertFalse(dictionary.contains("hello"));
    }

    @Test
    void testDictionaryGetRandomWord() {
        String word1 = dictionary.getRandomWord();
        String word2 = dictionary.getRandomWord();
        assertNotNull(word1);
        assertNotNull(word2);
        assertEquals(5, word1.length());
        assertEquals(5, word2.length());
        assertTrue(dictionary.contains(word1));
        assertTrue(dictionary.contains(word2));
    }

    @Test
    void testDictionarySize() {
        assertEquals(18, dictionary.size());
    }

    @Test
    void testDictionaryGetWordsReturnsCopy() {
        List<String> words = dictionary.getWords();
        words.clear();
        assertEquals(18, dictionary.size());
    }

    @Test
    void testAnalyzeWordExactMatch() {
        String result = WordleDictionary.analyzeWord("абзац", "абзац");
        assertEquals("+++++", result);
    }

    @Test
    void testAnalyzeWordWrongPosition() {
        String result = WordleDictionary.analyzeWord("абзац", "цабза");
        assertEquals("^^^^^", result);
    }

    @Test
    void testAnalyzeWordMixed() {
        String result = WordleDictionary.analyzeWord("абзац", "автор");
        assertEquals("+----", result);
    }

    @Test
    void testAnalyzeWordInvalidLength() {
        String result = WordleDictionary.analyzeWord("мама", "мама");
        assertNull(result);

        result = WordleDictionary.analyzeWord("мамам", "мама");
        assertNull(result);
    }

    @Test
    void testGameInitialization() {
        assertNotNull(game);
        assertEquals(6, game.getMaxSteps());
        assertEquals(6, game.getSteps());
        assertFalse(game.isGameOver());
        assertFalse(game.isWon());
        assertNotNull(game.getAnswer());
        assertEquals(5, game.getAnswer().length());
        assertTrue(dictionary.contains(game.getAnswer()));
    }

    @Test
    void testGameMakeTurnExactMatch() throws Exception {
        String answer = game.getAnswer();
        assertNotNull(answer, "Ответ не должен быть null");
        assertEquals(5, answer.length(), "Ответ должен быть длиной 5 символов");

        WordleGame.TurnResult result = game.makeTurn(answer);

        assertEquals("+++++", result.getResult());
        assertEquals(5, result.getRemainingSteps());
        assertTrue(result.isGameOver());
        assertTrue(result.isWon());
        assertTrue(game.isGameOver());
        assertTrue(game.isWon());
    }

    @Test
    void testGameMakeTurnWrongWord() throws Exception {
        String wrongWord = "абзац";
        String answer = game.getAnswer();
        if (wrongWord.equals(answer)) {
            wrongWord = "автор";
        }

        WordleGame.TurnResult result = game.makeTurn(wrongWord);

        assertNotNull(result.getResult());
        assertEquals(5, result.getResult().length());
        assertEquals(5, result.getRemainingSteps());
        assertFalse(result.isGameOver());
        assertFalse(result.isWon());

        List<String> history = game.getHistory();
        assertEquals(1, history.size());
        assertEquals(wrongWord, history.get(0));

        List<String> results = game.getHistoryResults();
        assertEquals(1, results.size());
    }

    @Test
    void testGameMakeTurnInvalidLength() {
        assertThrows(InvalidWordLengthException.class, () -> {
            game.makeTurn("привет");
        });

        assertThrows(InvalidWordLengthException.class, () -> {
            game.makeTurn("дом");
        });

        assertThrows(InvalidWordLengthException.class, () -> {
            game.makeTurn("");
        });
    }

    @Test
    void testGameMakeTurnWordNotFound() {
        assertThrows(WordNotFoundException.class, () -> {
            game.makeTurn("абвгд");
        });

        assertThrows(WordNotFoundException.class, () -> {
            game.makeTurn("hello");
        });
    }

    @Test
    void testGameLose() throws Exception {
        int turns = 0;
        String answer = game.getAnswer();

        for (String word : dictionary.getWords()) {
            if (!word.equals(answer) && !game.isGameOver()) {
                try {
                    game.makeTurn(word);
                    turns++;
                } catch (Exception e) {
                }
                if (turns >= 6) {
                    break;
                }
            }
        }

        assertTrue(game.isGameOver());
        assertFalse(game.isWon());
        assertEquals(0, game.getSteps());
    }

    @Test
    void testGameCantMakeTurnAfterGameOver() throws Exception {
        String answer = game.getAnswer();
        assertNotNull(answer);
        game.makeTurn(answer);

        assertThrows(IllegalStateException.class, () -> {
            game.makeTurn("абзац");
        });
    }

    @Test
    void testHintFirstTurn() {
        String hint = game.getHint();
        assertNotNull(hint);
        assertEquals(5, hint.length());
        assertTrue(dictionary.contains(hint));
    }

    @Test
    void testHintAfterWrongGuesses() throws Exception {
        String word1 = "абзац";
        String word2 = "автор";
        String answer = game.getAnswer();

        if (word1.equals(answer)) word1 = "акция";
        if (word2.equals(answer)) word2 = "алмаз";

        if (dictionary.contains(word1) && !word1.equals(answer)) {
            game.makeTurn(word1);
        }
        if (dictionary.contains(word2) && !word2.equals(answer)) {
            game.makeTurn(word2);
        }

        String hint = game.getHint();
        if (hint != null) {
            assertEquals(5, hint.length());
            assertTrue(dictionary.contains(hint));
            List<String> history = game.getHistory();
            assertFalse(history.contains(hint));
        }
    }

    @Test
    void testHintRespectsCorrectPositions() throws Exception {
        String answer = game.getAnswer();
        assertNotNull(answer);
        char firstLetter = answer.charAt(0);
        boolean foundWord = false;

        for (String word : dictionary.getWords()) {
            if (word.charAt(0) == firstLetter && !word.equals(answer)) {
                try {
                    game.makeTurn(word);
                    foundWord = true;
                    break;
                } catch (Exception e) {
                }
            }
        }

        if (foundWord) {
            String hint = game.getHint();
            if (hint != null) {
                assertEquals(firstLetter, hint.charAt(0));
            }
        }
    }

    @Test
    void testHintRespectsIncorrectPositions() throws Exception {
        String answer = game.getAnswer();
        assertNotNull(answer);
        String word = null;

        for (String w : dictionary.getWords()) {
            if (w.charAt(0) != answer.charAt(0) && !w.equals(answer)) {
                word = w;
                break;
            }
        }

        if (word != null && dictionary.contains(word) && !word.equals(answer)) {
            try {
                game.makeTurn(word);
                String hint = game.getHint();
                if (hint != null) {
                    assertFalse(hint.startsWith(String.valueOf(answer.charAt(0))));
                }
            } catch (Exception e) {
            }
        }
    }

    @Test
    void testHintNoCandidates() throws Exception {
        for (int i = 0; i < 5 && !game.isGameOver(); i++) {
            String word = game.getHint();
            if (word != null && dictionary.contains(word) && !game.getHistory().contains(word)) {
                try {
                    game.makeTurn(word);
                } catch (Exception e) {
                }
            }
        }

        String hint = game.getHint();
        if (!game.isGameOver()) {
            assertNull(hint);
        }
    }

    @Test
    void testHintDoesntRepeatWords() throws Exception {
        String firstHint = game.getHint();
        if (firstHint != null) {
            String answer = game.getAnswer();
            if (dictionary.contains(firstHint) && !firstHint.equals(answer)) {
                try {
                    game.makeTurn(firstHint);
                } catch (Exception e) {
                }
            }

            String secondHint = game.getHint();
            if (secondHint != null && !game.isGameOver()) {
                assertNotEquals(firstHint, secondHint);
            }
        }
    }

    @Test
    void testTurnResultContainsAllInfo() throws Exception {
        String answer = game.getAnswer();
        assertNotNull(answer);
        WordleGame.TurnResult result = game.makeTurn(answer);

        assertEquals("+++++", result.getResult());
        assertEquals(5, result.getRemainingSteps());
        assertTrue(result.isGameOver());
        assertTrue(result.isWon());

        WordleGame.TurnResult result2 = new WordleGame.TurnResult("++---", 4, false, false);
        assertEquals("++---", result2.getResult());
        assertEquals(4, result2.getRemainingSteps());
        assertFalse(result2.isGameOver());
        assertFalse(result2.isWon());
    }

    @Test
    void testGameStateConsistency() throws Exception {
        String word1 = "абзац";
        String word2 = "автор";
        String answer = game.getAnswer();

        if (word1.equals(answer)) word1 = "акция";
        if (word2.equals(answer)) word2 = "алмаз";

        if (dictionary.contains(word1) && !word1.equals(answer)) {
            game.makeTurn(word1);
        }
        if (dictionary.contains(word2) && !word2.equals(answer)) {
            game.makeTurn(word2);
        }

        assertTrue(game.getSteps() >= 0 && game.getSteps() <= 6);
    }

    @Test
    void testWordleDictionaryLoaderException() {
        WordleDictionaryLoader loader = new WordleDictionaryLoader();

        assertThrows(DictionaryLoadException.class, () -> {
            loader.loadDictionary("nonexistent.txt");
        });
    }

    @Test
    void testWordleGameEmptyDictionaryException() {
        WordleDictionary emptyDict = new WordleDictionary(Arrays.asList());
        assertThrows(EmptyDictionaryException.class, () -> {
            new WordleGame(emptyDict);
        });
    }

    @Test
    void testGameWithNullDictionary() {
        assertThrows(EmptyDictionaryException.class, () -> {
            new WordleGame(null);
        });
    }
}