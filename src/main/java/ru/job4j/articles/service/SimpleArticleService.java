package ru.job4j.articles.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.articles.model.Article;
import ru.job4j.articles.model.Word;
import ru.job4j.articles.service.generator.ArticleGenerator;
import ru.job4j.articles.store.Store;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class SimpleArticleService implements ArticleService {

    private static final int BATCH_SIZE = 300_000;
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleArticleService.class.getSimpleName());

    private final ArticleGenerator articleGenerator;

    public SimpleArticleService(ArticleGenerator articleGenerator) {
        this.articleGenerator = articleGenerator;
    }

    @Override
    public void generate(Store<Word> wordStore, int count, Store<Article> articleStore) {
        LOGGER.info("Генерация статей в количестве {}", count);
        var words = wordStore.findAll();
        var batch = new ArrayList<Article>(BATCH_SIZE);
        IntStream.range(0, count).forEach(i -> {
            LOGGER.info("Сгенерирована статья № {}", i);
            var article = articleGenerator.generate(words);
            batch.add(article);
            if (batch.size() >= BATCH_SIZE) {
                saveBatch(batch, articleStore);
                batch.clear();
            }
        });
        if (!batch.isEmpty()) {
            saveBatch(batch, articleStore);
            batch.clear();
        }
    }

    private void saveBatch(List<Article> batch, Store<Article> articleStore) {
        LOGGER.info("Сохранение партии из {} статей", batch.size());
        batch.forEach(articleStore::save);
    }
}
