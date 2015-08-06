package io.corbel.resources.rem.i18n;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import io.corbel.resources.rem.i18n.model.I18n;
import com.google.common.collect.Sets;

public class I18nBaseRemTest {
    private static final String LARGE_LANGUAGE = "en,gb;q=0.5,es;q=0.2";

    private I18nBaseRem i18nBaseRem;


    @Before
    public void setup() {
        i18nBaseRem = new I18nBaseRem() {
            @Override
            public Class<I18n> getType() {
                return null;
            }
        };
    }

    private AbstractMap.SimpleEntry<String, List<String>> getEntry(String lang, List<String> expected) {
        return new AbstractMap.SimpleEntry(lang, expected);
    }

    @Test
    public void testGetCollection() {
        Set<AbstractMap.SimpleEntry<String, List<String>>> testData = Sets.newHashSet(getEntry("en", Arrays.asList("en")),
                getEntry("es;q=0.5,en", Arrays.asList("en", "es")),
                getEntry("es,en,uk;q=0.5,fr;q=0.2", Arrays.asList("es", "en", "uk", "fr")),
                getEntry("es,en,uk;q=0.2,fr;q=0.5", Arrays.asList("es", "en", "fr", "uk")), getEntry("", Arrays.asList()),
                getEntry("es,en,uk;q=0.2,fr;q=0=.5", Arrays.asList("es", "en", "uk", "fr")));

        testData.forEach((entry) -> {
            String languageHeader = entry.getKey();
            List<String> expectedList = entry.getValue();
            List<String> processedLanguage = i18nBaseRem.getProcessedLanguage(languageHeader);
            assertThat(processedLanguage).isEqualTo(expectedList);
        });


    }


}
