package com.caseystella.nlp;

import com.google.common.collect.Iterables;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: cstella
 * Date: 6/6/13
 * Time: 7:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class Tokenizer extends org.apache.lucene.analysis.Tokenizer{
    private Iterator<TaggedWord> tagged;
    private PositionIncrementAttribute posIncr;
    private TaggedWord currentWord;
    private TermAttribute termAtt;
    private boolean lemmaNext;
    private static final String MODEL="edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";

    public Tokenizer(Reader input) {
        super();
        MaxentTagger tagger = null;
        try {
            tagger = new MaxentTagger(MODEL);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        lemmaNext = false;
        posIncr = addAttribute(PositionIncrementAttribute.class);
        termAtt = addAttribute(TermAttribute.class);

        List<List<HasWord>> tokenized =
                MaxentTagger.tokenizeText(input);
        tagged = Iterables.concat(tagger.process(tokenized)).iterator();
    }
    /**
     * Consumers use this method to advance the stream to the next token.
     * The token stream emits inflected forms and lemmas interleaved (form1,
     * lemma1, form2, lemma2, etc.), giving lemmas and their inflected forms
     * the same PositionAttribute.
     *
     * The logic for this method was largely pulled from  https://github.com/larsmans/lucene-stanford-lemmatizer
     * by larsmans
     */
    @Override
    public final boolean incrementToken() throws IOException {
        if (lemmaNext) {
            // Emit a lemma
            posIncr.setPositionIncrement(1);
            String tag  = currentWord.tag();
            String form = currentWord.word();
            termAtt.setTermBuffer(Morphology.stemStatic(form, tag).word());
        } else {
            // Emit inflected form, if not filtered out.

            // 0 because the lemma will come in the same position
            int increment = 0;
            for (;;) {
                if (!tagged.hasNext())
                    return false;
                currentWord = tagged.next();
                if (includedPOS(currentWord.tag()))
                    break;
                increment++;
            }

            posIncr.setPositionIncrement(increment);
            termAtt.setTermBuffer(currentWord.word());
        }

        lemmaNext = !lemmaNext;
        return true;
    }

    protected boolean includedPOS(String tag) {
        return tag.startsWith("NN") || tag.startsWith("V") || tag.startsWith("J") || tag.startsWith("R");
    }
}
