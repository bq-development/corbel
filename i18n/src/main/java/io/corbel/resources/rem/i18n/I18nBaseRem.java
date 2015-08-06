package io.corbel.resources.rem.i18n;

import static java.util.Arrays.stream;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import io.corbel.resources.rem.BaseRem;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.i18n.model.I18n;
import io.corbel.resources.rem.service.RemService;
import com.google.common.base.Strings;

public abstract class I18nBaseRem extends BaseRem<I18n> {
    public static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";
    protected static final String ID_SEPARATOR = ":";

    private Rem jsonI18nRem;

    protected RemService remService;

    protected Rem getJsonRem(String type, HttpMethod httpMethod) {
        if (jsonI18nRem == null) {
            List<MediaType> list = Arrays.asList(MediaType.APPLICATION_JSON);
            jsonI18nRem = remService.getRem(type, list, httpMethod);
        }
        return jsonI18nRem;
    }

    public void setRemService(RemService remService) {
        this.remService = remService;
    }

    protected static String getLanguage(MultivaluedMap<String, String> headers) {
        return headers.getFirst(ACCEPT_LANGUAGE_HEADER);
    }

    protected static List<String> getProcessedLanguage(String languageField) {
        // "es,en-En;q=0.6,en;q=0.4" -> [es, en-En, en]
        return stream(languageField.split(",")).filter(entry -> !Strings.isNullOrEmpty(entry)).map(language -> {
            String[] entry = language.split(";");
            String lang = entry[0];
            double q;
            try {
                q = entry.length == 1 ? 1 : Double.parseDouble(entry[1].replace("q=", ""));
            } catch (Exception e) {
                q = -1;
            }
            return new AbstractMap.SimpleImmutableEntry<>(lang, q);
        }).sorted((a, b) -> b.getValue().compareTo(a.getValue())).map(entry -> entry.getKey()).collect(Collectors.toList());
    }

}
