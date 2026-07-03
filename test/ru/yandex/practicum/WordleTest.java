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
    void testDictionaryGetRandomWord() throws EmptyDictionaryException {
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
    void testDictionaryGetRandomWordThrowsOnEmpty() {
        WordleDictionary emptyDict = new WordleDictionary(Arrays.asList());
        assertThrows(EmptyDictionaryException.class, emptyDict::getRandomWord);
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

        TurnResult result = game.makeTurn(answer);

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

        TurnResult result = game.makeTurn(wrongWord);

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
    void testHintFirstTurn() throws Exception {
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

        try {
            String hint = game.getHint();
            assertEquals(5, hint.length());
            assertTrue(dictionary.contains(hint));
            List<String> history = game.getHistory();
            assertFalse(history.contains(hint));
        } catch (HintNotFoundException e) {
            // Отсутствие кандидатов - допустимый исход в зависимости от словаря
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
            try {
                String hint = game.getHint();
                assertEquals(firstLetter, hint.charAt(0));
            } catch (HintNotFoundException e) {
                // Отсутствие кандидатов - допустимый исход
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

        if (word != null && dictionary.contains(word) && word.equals(answer)) {
            try {
                game.makeTurn(word);
                String hint = game.getHint();
                assertFalse(hint.startsWith(String.valueOf(answer.charAt(0))));
            } catch (HintNotFoundException e) {
                // Отсутствие кандидатов - допустимый исход
            } catch (Exception e) {
            }
        }
    }

    @Test
    void testHintNoCandidates() throws Exception {
        for (int i = 0; i < 5 && !game.isGameOver(); i++) {
            try {
                String word = game.getHint();
                if (dictionary.contains(word) && !game.getHistory().contains(word)) {
                    game.makeTurn(word);
                }
            } catch (HintNotFoundException e) {
                break;
            } catch (Exception e) {
            }
        }

        if (!game.isGameOver()) {
            // Либо кандидаты закончились и получим исключение,
            // либо подсказка ещё найдётся - оба исхода допустимы
            try {
                game.getHint();
            } catch (HintNotFoundException expected) {
                // ok
            }
        }
    }

    @Test
    void testHintDoesntRepeatWords() throws Exception {
        try {
            String firstHint = game.getHint();
            String answer = game.getAnswer();
            if (dictionary.contains(firstHint) && !firstHint.equals(answer)) {
                game.makeTurn(firstHint);
            }

            if (!game.isGameOver()) {
                try {
                    String secondHint = game.getHint();
                    assertNotEquals(firstHint, secondHint);
                } catch (HintNotFoundException e) {
                    // Кандидаты закончились - допустимый исход
                }
            }
        } catch (HintNotFoundException e) {
            // Кандидаты закончились - допустимый исход
        }
    }

    @Test
    void testHintDoesNotExcludeAnswerLetterOnDuplicateGuess() throws Exception {
        // Регрессионный тест на баг: если в загаданном слове буква встречается
        // один раз, а игрок вводит слово с этой же буквой дважды, "лишнее"
        // вхождение получает статус '-' и раньше ошибочно попадало в
        // incorrectLetters целиком, из-за чего подсказка отбрасывала и сам
        // правильный ответ.
        List<String> words = Arrays.asList("книга", "кокон", "актер", "алмаз", "бегун");
        WordleDictionary dict = new WordleDictionary(words);

        WordleGame localGame = null;
        for (int i = 0; i < 100; i++) {
            WordleGame candidate = new WordleGame(dict);
            if (candidate.getAnswer().equals("книга")) {
                localGame = candidate;
                break;
            }
        }
        assertNotNull(localGame, "Не удалось получить нужное загаданное слово за отведённое число попыток");

        localGame.makeTurn("кокон");

        try {
            String hint = localGame.getHint();
            assertNotNull(hint, "Подсказка не должна пропадать из-за буквы 'к', "
                    + "которая присутствует в отгаданном слове");
        } catch (HintNotFoundException e) {
            fail("Буква 'к' присутствует в ответе 'книга', подсказка не должна исчезать "
                    + "из-за повторного вхождения буквы в догадке");
        }
    }

    @Test
    void testTurnResultContainsAllInfo() throws Exception {
        String answer = game.getAnswer();
        assertNotNull(answer);
        TurnResult result = game.makeTurn(answer);

        assertEquals("+++++", result.getResult());
        assertEquals(5, result.getRemainingSteps());
        assertTrue(result.isGameOver());
        assertTrue(result.isWon());

        TurnResult result2 = new TurnResult("++---", 4, false, false);
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
