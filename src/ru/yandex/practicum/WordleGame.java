package ru.yandex.practicum;

import java.util.*;

public class WordleGame {

    private static final int MAX_STEPS = 6;

    private final String answer;
    private int steps;
    private final WordleDictionary dictionary;
    private final Random random = new Random();

    private final List<String> history;
    private final List<String> historyResults;

    private final Set<Character> correctLetters;
    private final Set<Character> incorrectLetters;
    private final Map<Character, Set<Integer>> correctPositions;
    private final Map<Character, Set<Integer>> incorrectPositions;

    private boolean gameOver;
    private boolean won;

    public WordleGame(WordleDictionary dictionary) throws EmptyDictionaryException {
        if (dictionary == null || dictionary.size() == 0) {
            throw new EmptyDictionaryException("Словарь пуст или не загружен");
        }

        this.dictionary = dictionary;
        this.answer = dictionary.getRandomWord();
        this.steps = MAX_STEPS;
        this.history = new ArrayList<>();
        this.historyResults = new ArrayList<>();
        this.correctLetters = new HashSet<>();
        this.incorrectLetters = new HashSet<>();
        this.correctPositions = new HashMap<>();
        this.incorrectPositions = new HashMap<>();
        this.gameOver = false;
        this.won = false;
    }

    public TurnResult makeTurn(String word) throws WordNotFoundException, InvalidWordLengthException {
        if (gameOver) {
            throw new IllegalStateException("Игра уже завершена");
        }

        if (word == null || word.length() != WordleDictionary.WORD_LENGTH) {
            throw new InvalidWordLengthException(
                    "Слово должно состоять из " + WordleDictionary.WORD_LENGTH + " букв");
        }

        if (!dictionary.contains(word)) {
            throw new WordNotFoundException("Слово \"" + word + "\" не найдено в словаре");
        }

        steps--;
        String result = WordleDictionary.analyzeWord(word, answer);

        if (result == null) {
            throw new RuntimeException("Ошибка анализа слова");
        }

        history.add(word);
        historyResults.add(result);
        updateLetterInfo(word, result);

        if (result.equals("+++++")) {
            gameOver = true;
            won = true;
        } else if (steps == 0) {
            gameOver = true;
            won = false;
        }

        return new TurnResult(result, steps, gameOver, won);
    }

    private void updateLetterInfo(String word, String result) {

        Set<Character> presentThisTurn = new HashSet<>();
        for (int i = 0; i < word.length(); i++) {
            char status = result.charAt(i);
            if (status == '+' || status == '^') {
                presentThisTurn.add(word.charAt(i));
            }
        }

        for (int i = 0; i < word.length(); i++) {
            char letter = word.charAt(i);
            char status = result.charAt(i);

            if (status == '+') {
                correctLetters.add(letter);
                incorrectLetters.remove(letter);
                correctPositions.computeIfAbsent(letter, k -> new HashSet<>()).add(i);
            } else if (status == '^') {
                correctLetters.add(letter);
                incorrectLetters.remove(letter);
                incorrectPositions.computeIfAbsent(letter, k -> new HashSet<>()).add(i);
            } else if (status == '-') {
                if (!presentThisTurn.contains(letter)) {
                    incorrectLetters.add(letter);
                }
            }
        }
    }

    public String getHint() throws EmptyDictionaryException, HintNotFoundException {
        if (history.isEmpty()) {
            return dictionary.getRandomWord();
        }

        List<String> candidates = new ArrayList<>(dictionary.getWords());

        candidates.removeIf(word -> {
            for (char c : incorrectLetters) {
                if (word.indexOf(c) >= 0) {
                    return true;
                }
            }
            return false;
        });

        candidates.removeIf(word -> {
            for (char c : correctLetters) {
                if (word.indexOf(c) < 0) {
                    return true;
                }
            }
            return false;
        });

        candidates.removeIf(word -> {
            for (Map.Entry<Character, Set<Integer>> entry : correctPositions.entrySet()) {
                char letter = entry.getKey();
                for (int pos : entry.getValue()) {
                    if (word.charAt(pos) != letter) {
                        return true;
                    }
                }
            }
            return false;
        });

        candidates.removeIf(word -> {
            for (Map.Entry<Character, Set<Integer>> entry : incorrectPositions.entrySet()) {
                char letter = entry.getKey();
                for (int pos : entry.getValue()) {
                    if (word.charAt(pos) == letter) {
                        return true;
                    }
                }
            }
            return false;
        });

        candidates.removeAll(history);

        if (candidates.isEmpty()) {
            throw new HintNotFoundException("Нет подходящих слов для подсказки");
        }

        return candidates.get(random.nextInt(candidates.size()));
    }

    public String getAnswer() {
        return answer;
    }

    public int getSteps() {
        return steps;
    }

    public int getMaxSteps() {
        return MAX_STEPS;
    }

    public List<String> getHistory() {
        return new ArrayList<>(history);
    }

    public List<String> getHistoryResults() {
        return new ArrayList<>(historyResults);
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isWon() {
        return won;
    }
}
