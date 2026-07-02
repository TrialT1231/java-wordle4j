package ru.yandex.practicum;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WordleDictionaryLoader {

    public WordleDictionary loadDictionary(String fileName) throws DictionaryLoadException {
        List<String> words = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String normalized = normalizeWord(line.trim());
                if (normalized.length() == 5 && isRussianWord(normalized)) {
                    words.add(normalized);
                }
            }

        } catch (FileNotFoundException e) {
            throw new DictionaryLoadException("Файл словаря не найден: " + fileName, e);
        } catch (IOException e) {
            throw new DictionaryLoadException("Ошибка чтения файла словаря: " + fileName, e);
        }

        if (words.isEmpty()) {
            throw new DictionaryLoadException("Словарь пуст или не содержит слов длиной 5 букв", null);
        }

        return new WordleDictionary(words);
    }

    private String normalizeWord(String word) {
        return word.toLowerCase()
                .replace('ё', 'е')
                .replaceAll("\\s+", "");
    }

    private boolean isRussianWord(String word) {
        return word.matches("[а-я]+");
    }
}