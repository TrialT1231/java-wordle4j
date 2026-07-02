package ru.yandex.practicum;

import java.util.*;

public class WordleGame {
    private final String answer;
    private int steps;
    private final int maxSteps = 6;
    private final WordleDictionary dictionary;
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
        this.steps = maxSteps;
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

        if (word == null || word.length() != 5) {
            throw new InvalidWordLengthException("Слово должно состоять из 5 букв");
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
        for (int i = 0; i < 5; i++) {
            char letter = word.charAt(i);
            char status = result.charAt(i);

            if (status == '+') {
                correctLetters.add(letter);
                correctPositions.computeIfAbsent(letter, k -> new HashSet<>()).add(i);
            } else if (status == '^') {
                correctLetters.add(letter);
                incorrectPositions.computeIfAbsent(letter, k -> new HashSet<>()).add(i);
            } else if (status == '-') {
                incorrectLetters.add(letter);
            }
        }
    }

    public String getHint() {
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
            return null;
        }

        Random random = new Random();
        return candidates.get(random.nextInt(candidates.size()));
    }

    public String getAnswer() {
        return answer;
    }

    public int getSteps() {
        return steps;
    }

    public int getMaxSteps() {
        return maxSteps;
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

    public static class TurnResult {
        private final String result;
        private final int remainingSteps;
        private final boolean gameOver;
        private final boolean won;

        public TurnResult(String result, int remainingSteps, boolean gameOver, boolean won) {
            this.result = result;
            this.remainingSteps = remainingSteps;
            this.gameOver = gameOver;
            this.won = won;
        }

        public String getResult() {
            return result;
        }

        public int getRemainingSteps() {
            return remainingSteps;
        }

        public boolean isGameOver() {
            return gameOver;
        }

        public boolean isWon() {
            return won;
        }
    }
}