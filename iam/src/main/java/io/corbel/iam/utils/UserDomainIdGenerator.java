package io.corbel.iam.utils;

import com.google.common.base.Joiner;

public class UserDomainIdGenerator {

    private static final String SEPARATOR = ":";

    public static String generateDeviceId(String domain, String userId, String deviceUid){
        return Joiner.on(SEPARATOR).join(domain, userId, deviceUid);
    }
}
