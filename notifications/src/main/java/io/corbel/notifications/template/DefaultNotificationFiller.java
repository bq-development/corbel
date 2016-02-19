package io.corbel.notifications.template;

import io.corbel.notifications.model.NotificationTemplate;
import io.corbel.notifications.model.Template;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.codehaus.groovy.runtime.StringBufferWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.FieldFilter;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author Francisco Sanchez
 */
public class DefaultNotificationFiller implements NotificationFiller {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultNotificationFiller.class);

	private final MustacheFactory mustacheFactory = new DefaultMustacheFactory();

	@Override
	public NotificationTemplate fill(final NotificationTemplate notificationTemplate, final Map<String, String> properties) {
		ReflectionUtils.doWithFields(NotificationTemplate.class, new FieldCallback() {
			@Override
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				try {
					field.setAccessible(true);
					String template = field.get(notificationTemplate).toString();
					if (template != null) {
						String value = fillContent(field.get(notificationTemplate).toString(), properties);
						field.set(notificationTemplate, value);
					}
				} catch (IllegalAccessException ignored) {
					LOG.warn("Unaccessable template field ({}) in Notification.class. Ignoring it!", field.getName());
				}

			}
		}, field -> field.isAnnotationPresent(Template.class));
		return notificationTemplate;
	}

	private String fillContent(String content, Map<String, String> properties) {
		Mustache mustache = mustacheFactory.compile(new StringReader(content), content);
		StringBuffer buffer = new StringBuffer();
		StringBufferWriter writer = new StringBufferWriter(buffer);
        mustache.execute(writer, properties);
        String contentFilled = buffer.toString();
        return contentFilled.isEmpty() ? content : contentFilled;
	}
}
