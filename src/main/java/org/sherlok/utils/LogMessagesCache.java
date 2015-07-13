package org.sherlok.utils;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.reverse;
import static java.lang.String.format;

import java.util.Collection;
import java.util.Queue;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

import com.google.common.collect.EvictingQueue;

/**
 * Keeps the latest 1000 log messages in memory, by intercepting Logback's
 * messages.
 * 
 * @see http://logback.qos.ch/manual/filters.html
 * @author renaud@apache.org
 */
public class LogMessagesCache extends Filter<ILoggingEvent> {

    /** FIFO, fixed size (1000) */
    private static Queue<LogMessage> logCache = EvictingQueue.create(1000);

    /** accepts all messages, used for intercepting */
    @Override
    public FilterReply decide(ILoggingEvent event) {
        logCache.add(new LogMessage(event));
        return FilterReply.ACCEPT;
    }

    public static class LogMessage {
        private String message;
        private String level;

        public LogMessage(ILoggingEvent event) {
            this.message = event.getFormattedMessage();
            this.level = event.getLevel().toString();
        }

        public String getMessage() {
            return message;
        }

        public String getLevel() {
            return level;
        }

        @Override
        public String toString() {
            return format("[%s] %s", level, message);
        }
    }

    public static Collection<LogMessage> getLogMessages() {
        return reverse(newArrayList(logCache));
    }
}