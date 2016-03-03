package io.corbel.notifications.utils;

public class DomainNameIdGenerator {

    private static final String SEPARATOR = ":";

    public static String generateNotificationTemplateId(String domain, String name){
        return domain + SEPARATOR + name;
    }
}
