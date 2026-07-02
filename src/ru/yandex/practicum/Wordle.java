package ru.yandex.practicum;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Wordle {
    private static PrintWriter logger;

    public static void main(String[] args) {
        try {
            logger = new PrintWriter(
                    new OutputStreamWriter(new FileOutputStream("game.log"), StandardCharsets.UTF_8),
                    true
            );

            logMessage("=== Игра Wordle запущена ===");

            logMessage("Загрузка словаря...");
            WordleDictionaryLoader loader = new WordleDictionaryLoader();
            WordleDictionary dictionary = loader.loadDictionary("words_ru.txt");
            logMessage("Словарь загружен. Количество слов: " + dictionary.size());

            logMessage("Создание игры...");
            WordleGame game = new WordleGame(dictionary);
            logMessage("Игра создана. Загадано слово из " + dictionary.size() + " возможных");

            Scanner scanner = new Scanner(System.in);
            System.out.println("Добро пожаловать в игру Wordle!");
            System.out.println("Угадайте слово из 5 букв. У вас 6 попыток.");
            System.out.println("+ - буква на месте, ^ - буква есть, но не на месте, - - буквы нет");
            System.out.println("Для подсказки нажмите Enter без ввода слова");
            System.out.println();

            while (!game.isGameOver()) {
                System.out.println("Осталось попыток: " + game.getSteps());
                System.out.print("Введите слово: ");
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    String hint = game.getHint();
                    if (hint != null) {
                        System.out.println("Подсказка: " + hint);
                    } else {
                        System.out.println("Нет подходящих слов для подсказки");
                    }
                    continue;
                }

                String normalizedInput = normalizeInput(input);

                try {
                    WordleGame.TurnResult result = game.makeTurn(normalizedInput);

                    System.out.println(normalizedInput);
                    System.out.println(result.getResult());

                    logMessage("Ход: " + normalizedInput + " -> " + result.getResult() +
                            " (осталось: " + result.getRemainingSteps() + ")");

                    if (result.isGameOver()) {
                        if (result.isWon()) {
                            System.out.println("\nПоздравляем! Вы угадали слово: " + game.getAnswer());
                            logMessage("Игрок выиграл! Слово: " + game.getAnswer());
                        } else {
                            System.out.println("\nВы проиграли. Загаданное слово: " + game.getAnswer());
                            logMessage("Игрок проиграл. Слово: " + game.getAnswer());
                        }
                    }

                } catch (WordNotFoundException e) {
                    System.out.println("Ошибка: слово не найдено в словаре");
                    logMessage("Ошибка: " + e.getMessage());
                } catch (InvalidWordLengthException e) {
                    System.out.println("Ошибка: слово должно состоять из 5 букв");
                    logMessage("Ошибка: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("Произошла ошибка. Подробности в логе.");
                    logMessage("Непредвиденная ошибка: " + e.toString());
                    e.printStackTrace(logger);
                }
            }

        } catch (DictionaryLoadException e) {
            System.err.println("Критическая ошибка при загрузке словаря: " + e.getMessage());
            if (logger != null) {
                logMessage("Критическая ошибка: " + e.getMessage());
                e.printStackTrace(logger);
            }
        } catch (EmptyDictionaryException e) {
            System.err.println("Ошибка: " + e.getMessage());
            if (logger != null) {
                logMessage("Ошибка: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Непредвиденная ошибка: " + e.getMessage());
            if (logger != null) {
                logMessage("Непредвиденная ошибка: " + e.toString());
                e.printStackTrace(logger);
            }
        } finally {
            if (logger != null) {
                logMessage("=== Игра завершена ===");
                logger.close();
            }
        }
    }

    private static String normalizeInput(String input) {
        return input.toLowerCase()
                .replace('ё', 'е')
                .replaceAll("\\s+", "");
    }

    private static void logMessage(String message) {
        if (logger != null) {
            logger.println("[" + java.time.LocalDateTime.now() + "] " + message);
        }
    }
}