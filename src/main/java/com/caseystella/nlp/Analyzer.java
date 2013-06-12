package com.caseystella.nlp;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: cstella
 * Date: 6/6/13
 * Time: 7:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class Analyzer extends StopwordAnalyzerBase
{
    /** Default maximum allowed token length */
    public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

    private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;

    /**
     * Specifies whether deprecated acronyms should be replaced with HOST type.
     * See {@linkplain "https://issues.apache.org/jira/browse/LUCENE-1068"}
     */
    private boolean replaceInvalidAcronym;



    /** Builds an nlp with the given stop words.
     * @param matchVersion Lucene version to match See {@link
     * <a href="#version">above</a>}
     * @param stopWords stop words */
    public Analyzer(Version matchVersion, Set<?> stopWords) {
        super(matchVersion, stopWords);
        replaceInvalidAcronym = matchVersion.onOrAfter(Version.LUCENE_24);
        //MaxentTagger tagger = new MaxentTagger("edu/stanford/nlp/models/pos-tagger/
    }

    /** Builds an nlp with the default stop words .
     */
    public Analyzer() {
        this(Version.LUCENE_36, getStopwordsSet());
    }

    private static Set<?> getStopwordsSet()
    {
        final List<String> stopWords = Arrays.asList(
                "a", "an", "and", "are", "as", "at", "be", "but", "by",
                "for", "if", "in", "into", "is", "it",
                "no", "not", "of", "on", "or", "such",
                "that", "the", "their", "then", "there", "these",
                "they", "this", "to", "was", "will", "with"
                /*"the",
                "of",
                "to",
                "and",
                "in",
                "a",
                "that",
                "is",
                "palestinian",
                "for",
                "on",
                "be",
                "israeli",
                "with",
                "not",
                "the",
                "as",
                "will",
                "are",
                "it",
                "this",
                "by",
                "have",
                "israel",
                "has",
                "an",
                "from",
                "was",
                "palestinians",
                "their",
                "its",
                "or",
                "we",
                "at",
                "they",
                "political",
                "would",
                "his",
                "peace",
                "all",
                "which",
                "more",
                "international",
                "in",
                "only",
                "two",
                "there",
                "but",
                "i",
                "one",
                "no",
                "been",
                "this",
                "he",
                "sharon",
                "were",
                "can",
                "between",
                "who",
                "other",
                "these",
                "if",
                "year",
                "bush",
                "sharon",
                "but",
                "state",
                "it",
                "arab",
                "any",
                "israelis",
                "also",
                "q:",
                "security",
                "over",
                "even",
                "a:",
                "what",
                "than",
                "against",
                "into",
                "government",
                "do",
                "west",
                "united",
                "both",
                "some",
                "israel's",
                "arafat",
                "process",
                "about",
                "american",
                "should",
                "very",
                "people",
                "most",
                "new",
                "when",
                "because",
                "you",
                "jewish",
                "our",
                "must",
                "election",
                "us",
                "disengagement",
                "fence",
                "israeli-palestinian",
                "up",
                "administration",
                "roadmap"*/

                );
        final CharArraySet stopSet = new CharArraySet(Version.LUCENE_CURRENT,
                                            stopWords.size(), false);
        stopSet.addAll(stopWords);
        return CharArraySet.unmodifiableSet(stopSet);
    }
    @Override
    protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
        //final StandardTokenizer src = new StandardTokenizer(Version.LUCENE_36, reader);
        Tokenizer src = new Tokenizer(reader);
        TokenStream tok = new StandardFilter(matchVersion, src);
        tok = new LowerCaseFilter(matchVersion, tok);
        tok = new StopFilter(matchVersion, tok, stopwords);
        return new TokenStreamComponents(src, tok) {
            @Override
            protected boolean reset(final Reader reader) throws IOException {
                return super.reset(reader);
            }
        };
    }

}
