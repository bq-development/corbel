package io.corbel.notifications.service;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by Alberto J. Rubio
 */
public class SpringNotificationsServiceFactory implements ApplicationContextAware, NotificationsServiceFactory {

    private ApplicationContext applicationContext;

    @Override
    public NotificationsService getNotificationService(String type) {
        return applicationContext.getBean(type, NotificationsService.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
