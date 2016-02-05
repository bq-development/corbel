package io.corbel.resources.rem;


import io.corbel.resources.rem.service.RemService;

import java.io.InputStream;

public abstract class ImageBaseRem extends BaseRem <InputStream> {
    protected RemService remService;

    public void setRemService(RemService remService) {
        this.remService = remService;
    }
}
