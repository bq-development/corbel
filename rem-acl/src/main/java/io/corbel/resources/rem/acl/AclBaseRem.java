package io.corbel.resources.rem.acl;

import io.corbel.resources.rem.BaseRem;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.service.AclResourcesService;
import io.corbel.resources.rem.service.RemService;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.springframework.http.MediaType;

/**
 * @author Rubén Carrasco
 */
abstract public class AclBaseRem extends BaseRem<InputStream> {

    public static final List<MediaType> JSON_MEDIATYPE = Collections.singletonList(MediaType.APPLICATION_JSON);

    protected final AclResourcesService aclResourcesService;
    protected List<Rem> REMS_TO_EXCLUDE;
    protected RemService remService;

    public AclBaseRem(AclResourcesService aclResourcesService, List<Rem> remsToExclude) {
        this.aclResourcesService = aclResourcesService;
        this.REMS_TO_EXCLUDE = remsToExclude;
    }

    public void setRemService(RemService remService) {
        this.remService = remService;
    }

    @Override
    public Class<InputStream> getType() {
        return InputStream.class;
    }

}
