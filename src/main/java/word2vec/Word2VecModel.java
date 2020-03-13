package word2vec;

import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.CollectionSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Word2VecModel {

    public static List<List<Double>> transform(List<String> words) {
        SentenceIterator iter = new CollectionSentenceIterator(words);
        iter.setPreProcessor(String::toLowerCase);
        TokenizerFactory t = new DefaultTokenizerFactory();
        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(1)
                .iterations(1)
                .layerSize(20)
                .seed(42)
                .windowSize(5)
                .iterate(iter)
                .tokenizerFactory(t)
                .build();

        vec.fit();
        return words.stream()
                .filter(vec::hasWord)
                .map(w -> Arrays.stream(vec.getWordVector(w)).boxed().collect(Collectors.toList()))
                .collect(Collectors.toList());
    }
}
