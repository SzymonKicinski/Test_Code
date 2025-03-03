package com.dsv.datafactory.file.extraction;

import com.dsv.datafactory.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

// #TODO Profile from Spring?
// More precise exception handling?
// There is repeated deserialization code in the testPageDeserialization and testWords methods
// Unnecessary variables - condition


public class CDMSerializationTest {
    // #TODO better file names? Read below for an example
    // private static final String TEST_PAGE_PATH = "src/test/resources/CDMFiles/sample_page_data.json";
    // private static final String TEST_WORD_PATH_1 = "src/test/resources/CDMFiles/sample_word_data_1.json";
    // private static final String TEST_WORD_PATH_2 = "src/test/resources/CDMFiles/sample_word_data_2.json";
    String testPagePath = "src/test/resources/CDMFiles/page_test.json";
    String testWordPath1 = "src/test/resources/CDMFiles/word_test1.json";
    String testWordPath2 = "src/test/resources/CDMFiles/word_test2.json";


    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testPageDeserialization() throws IOException {
        Page page = objectMapper.readValue(new File(TEST_PAGE_PATH), Page.class);
        Page manualPage = loadTestPage(TEST_PAGE_PATH);

        Assertions.assertEquals(page.getWidth(), manualPage.getWidth());
        Assertions.assertEquals(page.getHeight(), manualPage.getHeight());
        Assertions.assertEquals(page.getRotation(), manualPage.getRotation());
        Assertions.assertEquals(page.getPageKey(), manualPage.getPageKey());
        Assertions.assertEquals(page.getPageNumber(), manualPage.getPageNumber());
    }


    private Page loadTestPage(String path) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(new File(path));
        Page page = new Page();
        page.setHeight(jsonNode.get("height").asInt());
        page.setWidth(jsonNode.get("width").asInt());
        page.setPageKey(jsonNode.get("pageKey").asText());
        page.setPageNumber(jsonNode.get("pageNumber").asInt());
        page.setRotation(jsonNode.get("rotation").asInt());
        page.setLines(getLines(jsonNode.get("lines")));
        page.setLanguage(getLanguage(jsonNode.get("language")));
        return page;
    }
    @Test
    void testWords() throws IOException {
        Word word1 = objectMapper.readValue(new File(TEST_WORD_PATH_1), Word.class);
        Word word2 = objectMapper.readValue(new File(TEST_WORD_PATH_2), Word.class);

        // Using try-with-resources: Used Files.readString(Path.of(...))
        // to read files, which automatically manages resources.
        Word manualWord1 = getSingleWord(Files.readString(Path.of(TEST_WORD_PATH_1)));
        Word manualWord2 = getSingleWord(Files.readString(Path.of(TEST_WORD_PATH_2)));

        assertWordsEqual(word1, manualWord1);
        assertWordsEqual(word2, manualWord2);
    }

    // Helper method assertWordsEqual: Created a helper method to
// compare Word objects to reduce duplicate code.
// You could go as far as to move this method to the utilTest package
// if the method would be used in other tests -> to discus with the team ?
    private void assertWordsEqual(Word expected, Word actual) {
        Assertions.assertEquals(expected.getRotation(), actual.getRotation());
        Assertions.assertEquals(expected.getWord(), actual.getWord());
        Assertions.assertEquals(expected.getConfidence(), actual.getConfidence());
        Assertions.assertEquals(expected.getxMean(), actual.getxMean());
        Assertions.assertEquals(expected.getyMean(), actual.getyMean());
    }

    private List<Language> getLanguage(JsonNode pageNode) {
        List<Language> languages = new ArrayList<>();
        for (JsonNode obj : pageNode) {
            Language lang = new Language();
            lang.setLanguageCode(obj.get("languageCode").asText());
            lang.setConfidence((float) obj.get("confidence").asDouble());
            languages.add(lang);
        }
        return languages;
    }

    private List<Line> getLines(JsonNode pageNode) {
        List<Line> lines = new ArrayList<>();
        for (JsonNode obj : pageNode) {
            Line line = new Line();
            line.setLineNumber(obj.get("lineNumber").asInt());
            line.setWords(getWordsFromJson(obj.get("words")));
            lines.add(line);
        }
        return lines;
    }

    private List<Word> getWordsFromJson(JsonNode lineWords) {
        List<Word> words = new ArrayList<>();
        if (lineWords.isArray()) {
            for (JsonNode obj : lineWords) {
                words.add(getSingleWord(obj));
            }
        } else {
            words.add(getSingleWord(lineWords));
        }
        return words;
    }

    private Word getSingleWord(JsonNode word) {
        Word newWord = new Word();
        newWord.setWord(word.get("word").asText());
        newWord.setConfidence(word.get("confidence").asInt());
        newWord.setRotation(word.get("rotation").asInt());
        newWord.setxMean(word.get("xMean").asInt());
        newWord.setyMean(word.get("yMean").asInt());
        newWord.setBoundingBox(getBoundingBox(word.get("boundingBox")));
        newWord.setTopLeftCorner(getVertice(word.get("topLeftCorner")));
        newWord.setLowLeftCorner(getVertice(word.get("lowLeftCorner")));
        newWord.setTopRightCorner(getVertice(word.get("topRightCorner")));
        newWord.setLowRightCorner(getVertice(word.get("lowRightCorner")));
        return newWord;
    }

    private BoundingBox getBoundingBox(JsonNode bbox) {
        return new BoundingBox(bbox.get("x1").asInt(), bbox.get("x2").asInt(), bbox.get("y1").asInt(), bbox.get("y2").asInt());
    }

    private Vertices getVertice(JsonNode vertice) {
        return new Vertices(vertice.get("x").asInt(), vertice.get("y").asInt());
    }
}