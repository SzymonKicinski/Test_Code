package com.dsv.datafactory.file.extraction.processor.domain.ocr;

import com.dsv.datafactory.file.extraction.processor.domain.ocr.parsers.Parsers;
import com.dsv.datafactory.file.extraction.processor.models.*;
import com.dsv.datafactory.file.extraction.processor.models.BoundingPoly;
import com.dsv.datafactory.file.extraction.processor.models.EntityAnnotation;
import com.dsv.datafactory.file.extraction.processor.models.TextAnnotation;
import com.google.cloud.vision.v1.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


// Adding spring
// Adding try-resources-catch
// Adding Optional
// Considering whether the approach of writing an Orchestrator for CORs is a good idea?
// If Orchestrators were only used by OCR, then limit other classes from accessing them?
// Maybe using List instead of ArrayList? -> More flexibility in the future
// Using streams
// Extracting parsers to another class -> Possible future expansion of parsers with others
// Maybe elevate the class one floor higher to another package so that it is clear what the purpose of the class is
// to prevent null values in parameters in method we shoudl add some checking of values to not get a null / nullPointerException
public class OcrParser {
    private final AnnotateImageResponse raw;

    // @Autowired maybe to be added?
    public OcrParser(AnnotateImageResponse response) {
        this.raw = response;
    }

    public GoogleVisionResponse parse() {
        GoogleVisionResponse parsed = new GoogleVisionResponse();
        parsed.setTextAnnotations(parseTextAnnotations(raw.getTextAnnotationsList()));
        parsed.setFullTextAnnotation(parseFullTextAnnotation(raw.getFullTextAnnotation()));

        return parsed;
    }

    public ArrayList<EntityAnnotation> parseTextAnnotations(List<com.google.cloud.vision.v1.EntityAnnotation> rawTextAnnotations) {
        return rawTextAnnotations.stream()
                .map(this::parseEntityAnnotation)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public EntityAnnotation parseEntityAnnotation(com.google.cloud.vision.v1.EntityAnnotation rawEntity) {
        BoundingPoly parsedPoly = parseBoundingPoly(rawEntity.getBoundingPoly());
        return new EntityAnnotation(rawEntity.getLocale(), rawEntity.getDescription(), rawEntity.getConfidence(), parsedPoly);
    }

    public TextAnnotation parseFullTextAnnotation(com.google.cloud.vision.v1.TextAnnotation fullTextAnnotation) {
        TextAnnotation parsedTextAnnotation = new TextAnnotation();
        parsedTextAnnotation.setText(fullTextAnnotation.getText());
        parsedTextAnnotation.setPages(parsePages(fullTextAnnotation.getPagesList()));
        return parsedTextAnnotation;
    }

    public ArrayList<GooglePage> parsePages(List<Page> rawPages) {
        return rawPages.stream()
                .map(this::parsePage)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private GooglePage parsePage(Page rawPage) {
        GooglePage parsed = new GooglePage();
        parsed.setConfidence(rawPage.getConfidence());
        parsed.setHeight(rawPage.getHeight());
        parsed.setWidth(rawPage.getWidth());
        parsed.setBlocks(parseBlocks(rawPage.getBlocksList()));
        parsed.setTextProperty(parseTextProperty(rawPage.getProperty()));
        return parsed;
    }

    public BoundingPoly parseBoundingPoly(com.google.cloud.vision.v1.BoundingPoly rawBoundingPoly) {

        BoundingPoly parsedPoly = new BoundingPoly(); // Potential NullPointerException -
        // No constructor in the model - we use the default one from Java
        ArrayList<Vertices> vertices = rawBoundingPoly.getVerticesList().stream()
                .map(rawVertex -> new Vertices(rawVertex.getX(), rawVertex.getY()))
                .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<NormalizedVertices> normVertices = rawBoundingPoly.getNormalizedVerticesList().stream()
                .map(rawVertex -> new NormalizedVertices(rawVertex.getX(), rawVertex.getY()))
                .collect(Collectors.toCollection(ArrayList::new));
        parsedPoly.setVertices(vertices);
        parsedPoly.setNormalizedVertices(normVertices);
        return parsedPoly;
    }

    public TextProperty parseTextProperty(com.google.cloud.vision.v1.TextAnnotation.TextProperty rawTextProperty) {
        TextProperty property = new TextProperty();
        DetectedBreak detectedBreak = new DetectedBreak(
                rawTextProperty.getDetectedBreak().getType().name(),
                rawTextProperty.getDetectedBreak().getTypeValue(),
                rawTextProperty.getDetectedBreak().getIsPrefix()
        );
        property.setDetectedBreak(detectedBreak);
        ArrayList<DetectedLanguage> detectedLanguages = rawTextProperty.getDetectedLanguagesList().stream()
                .map(rawDL -> new DetectedLanguage(rawDL.getConfidence(), rawDL.getLanguageCode()))
                .collect(Collectors.toCollection(ArrayList::new));
        property.setDetectedLanguages(detectedLanguages);
        return property;
    }

    public ArrayList<GoogleBlock> parseBlocks(List<Block> rawBlocks) {
        return rawBlocks.stream()
                .map(this::parseBlock)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private GoogleBlock parseBlock(Block rawBlock) {
        GoogleBlock block = new GoogleBlock();
        block.setBlockType(rawBlock.getBlockType().name());
        block.setConfidence(rawBlock.getConfidence());
        block.setBoundingBox(parseBoundingPoly(rawBlock.getBoundingBox()));
        block.setProperty(parseTextProperty(rawBlock.getProperty()));
        block.setParagraphs(parseParagraphs(rawBlock.getParagraphsList()));
        return block;
    }

    private ArrayList<GoogleParagraph> parseParagraphs(List<Paragraph> rawParagraphs) {
        return rawParagraphs.stream()
                .map(this::parseParagraph)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private GoogleParagraph parseParagraph(Paragraph rawParagraph) {
        GoogleParagraph paragraph = new GoogleParagraph();
        paragraph.setBoundingBox(parseBoundingPoly(rawParagraph.getBoundingBox()));
        paragraph.setConfidence(rawParagraph.getConfidence());
        paragraph.setProperty(parseTextProperty(rawParagraph.getProperty()));
        paragraph.setWords(parseWords(rawParagraph.getWordsList()));
        return paragraph;
    }

    private ArrayList<GoogleWord> parseWords(List<Word> rawWords) {
        return rawWords.stream()
                .map(this::parseWord)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private GoogleWord parseWord(Word rawWord) {
        GoogleWord word = new GoogleWord();
        word.setConfidence(rawWord.getConfidence());
        word.setProperty(parseTextProperty(rawWord.getProperty()));
        word.setBoundingBox(parseBoundingPoly(rawWord.getBoundingBox()));
        word.setSymbols(parseSymbols(rawWord.getSymbolsList()));
        return word;
    }

    public ArrayList<GoogleSymbol> parseSymbols(List<Symbol> rawSymbols) {
        return rawSymbols.stream()
                .map(this::parseSymbol)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private GoogleSymbol parseSymbol(Symbol rawSymbol) {
        GoogleSymbol symbol = new GoogleSymbol();
        symbol.setBoundingBox(parseBoundingPoly(rawSymbol.getBoundingBox()));
        symbol.setText(rawSymbol.getText());
        symbol.setConfidence(rawSymbol.getConfidence());
        symbol.setProperty(parseTextProperty(rawSymbol.getProperty()));
        return symbol;
    }

}
