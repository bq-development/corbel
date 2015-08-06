package io.corbel.resources.rem.i18n.ioc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.i18n.I18nDeleteRem;
import io.corbel.resources.rem.i18n.I18nGetRem;
import io.corbel.resources.rem.i18n.I18nPutRem;
import io.corbel.lib.config.ConfigurationIoC;
import com.google.gson.Gson;


@Configuration @Import({ConfigurationIoC.class}) public class I18nIoc {

    @Bean(name = I18nRemNames.I18N_GET)
    public Rem getI18nGetRem() {
        return new I18nGetRem();
    }

    @Bean(name = I18nRemNames.I18N_PUT)
    public Rem getI18nPutRem(Gson gson) {
        return new I18nPutRem(gson);
    }

    @Bean(name = I18nRemNames.I18N_DELETE)
    public Rem getI18nDeleteRem(Gson gson) {
        return new I18nDeleteRem();
    }

    @Bean
    @Lazy(true)
    public Gson getGson() {
        return new Gson();
    }

}
