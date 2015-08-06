package io.corbel.evci.eworker;

import io.corbel.evci.eworker.plugin.EworkerPlugin;

/**
 * Eworker: Events workers module
 *
 * This modules are used to delegate the resolution of certain evci events.
 *
 * Eworkers must be registered via the {@link EworkerRegistry} during the execution of the {@link EworkerPlugin}
 * implementation of this Eworker.
 *
 * @author Alberto J. Rubio
 *
 */
public interface Eworker<E> {

	void handleMessage(E message);

	default void handleFailedMessage(E message) {
		//by default failed messages are ignored
	}

}
