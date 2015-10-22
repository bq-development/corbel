package io.corbel.resources.rem.ioc;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.corbel.lib.config.ConfigurationIoC;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.acl.*;
import io.corbel.resources.rem.service.AclResourcesService;
import io.corbel.resources.rem.service.DefaultAclResourcesService;

/**
 * @author Cristian del Cerro
 */

@Configuration @Import({ConfigurationIoC.class}) public class RemAclIoc {

    @Bean(name = AclRemNames.POST)
    public Rem getAclPostRem() {
        return new AclPostRem(getAclResourceService(), Arrays.asList(getAclPutRem()));
    }

    @Bean(name = AclRemNames.GET)
    public Rem getAclGetRem() {
        return new AclGetRem(getAclResourceService());
    }

    @Bean(name = AclRemNames.PUT)
    public Rem getAclPutRem() {
        return new AclPutRem(getAclResourceService(), Arrays.asList(getAclGetRem()));
    }

    @Bean(name = AclRemNames.DELETE)
    public Rem getAclDeleteRem() {
        return new AclDeleteRem(getAclResourceService(), Arrays.asList(getAclGetRem()));
    }

    @Bean(name = AclRemNames.SETUP_PUT)
    public Rem getAclSetUpPutRem() {
        return new SetUpAclPutRem(getAclResourceService(), Arrays.asList(getAclGetRem(), getAclPutRem()));
    }

    @Bean
    public AclResourcesService getAclResourceService() {
        return new DefaultAclResourcesService();
    }

}
