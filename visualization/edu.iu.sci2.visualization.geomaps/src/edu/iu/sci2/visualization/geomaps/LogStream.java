package edu.iu.sci2.visualization.geomaps;

import java.io.PrintStream;
import java.util.Formatter;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

import com.google.common.base.Preconditions;

/**
 * A bridge to the bundle's {@link LogService} or, when not available, a fallback PrintStream (one
 * of {@link System#err} or {@link System#out}.
 */
public enum LogStream {
	/**
	 * Access to the backing LogService using severity level {@link LogService#LOG_ERROR} or, when
	 * a LogService is not available, {@link System#err}.
	 */
	ERROR(LogService.LOG_ERROR, System.err),
	/**
	 * Access to the backing LogService using severity level {@link LogService#LOG_WARNING} or,
	 * when a LogService is not available, {@link System#out}.
	 */
	WARNING(LogService.LOG_WARNING, System.out),
	/**
	 * Access to the backing LogService using severity level {@link LogService#LOG_INFO} or, when a
	 * LogService is not available, {@link System#out}.
	 */
	INFO(LogService.LOG_INFO, System.out),
	/**
	 * Access to the backing LogService using severity level {@link LogService#LOG_DEBUG} or, when
	 * a LogService is not available, {@link System#out}.
	 */
	DEBUG(LogService.LOG_DEBUG, System.out);

	private static LogService logService;

	private final int severityLevel;
	private final transient PrintStream fallback;

	private LogStream(int severityLevel, PrintStream fallback) {
		this.severityLevel = severityLevel;
		this.fallback = fallback;
	}

	private static void setService(LogService logService) {
		LogStream.logService = logService;
	}

	/**
	 * @param messageTemplate
	 *            A {@link Formatter format string} for the human readable message describing the
	 *            condition.
	 * @param templateArguments
	 *            Arguments referenced by the format specifiers in the format string.
	 * @see #send(Throwable, String, Object...)
	 */
	public void send(String messageTemplate, Object... templateArguments) {
		// TODO Are these send signatures ambiguous?  What if I call the Throwable variant with null?
		send((Throwable) null, messageTemplate, templateArguments);
	}
	
	/**
	 * Logs an throwable and its message.
	 * 
	 * <p>
	 * This information is sent to a backing {@link LogService} when available, otherwise to a
	 * standard output stream like {@link System#err} or {@link System#out}.
	 * 
	 * @param throwable
	 *            The throwable that reflects the condition.
	 * 
	 * @see #send(Throwable, String, Object...)
	 */
	public void send(Throwable throwable) {
		Preconditions.checkNotNull(throwable);
		
		send(throwable, throwable.getMessage());
	}	
	
	/**
	 * Logs a message and an throwable.
	 * 
	 * <p>
	 * This information is sent to a backing {@link LogService} when available, otherwise to a
	 * standard output stream like {@link System#err} or {@link System#out}.
	 * 
	 * @param throwable
	 *            The throwable that reflects the condition or null.
	 * @param messageTemplate
	 *            A {@link Formatter format string} for the human readable message describing the
	 *            condition.
	 * @param templateArguments
	 *            Arguments referenced by the format specifiers in the format string.
	 * 
	 * @see LogService#log(int, String, Throwable)
	 */
	public void send(Throwable throwable, String messageTemplate, Object... templateArguments) {
		Preconditions.checkNotNull(messageTemplate);
		
		String message = String.format(messageTemplate, templateArguments);
		
		if (logService != null) {
			// Send to log service
			logService.log(severityLevel, message, throwable);
		} else {
			// Send to fallback
			fallback.println(String.format("%s: %s", this.toString(), message));
	
			if (throwable != null) {
				fallback.println("The exception reflecting the condition: " + throwable);
			}
		}
	}	
	
	
	/**
	 * Sets (or unsets) the {@link LogService} on {@link LogStream} during activation
	 * (or deactivation).
	 */
	public static final class Activator implements BundleActivator {
		@Override
		public void start(BundleContext context) {
			LogStream.setService((LogService) context.getService(
					context.getServiceReference(LogService.class.getName())));
		}

		@Override
		public void stop(BundleContext context) { // TODO Right?
			context.ungetService(context.getServiceReference(LogService.class.getName()));

			// The service's service object should no longer be used and all references to it should be
			// destroyed when a bundle's use count for the service drops to zero.
			LogStream.setService(null);
		}
	}
}
