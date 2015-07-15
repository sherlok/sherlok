package org.sherlok.utils;

import static java.lang.System.currentTimeMillis;
import static org.slf4j.LoggerFactory.getLogger;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.MetadataNotFoundException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferResource;
import org.slf4j.Logger;

/**
 * A transfer listener that logs downloads.
 * 
 * @author renaud@apache.org
 */
public class ConsoleTransferListener extends AbstractTransferListener {
    private static Logger LOG = getLogger(ConsoleTransferListener.class);

    private static final long ONE_SECONDS = 1000;
    private static final DecimalFormat FORMAT = new DecimalFormat("0.0",
            new DecimalFormatSymbols(Locale.ENGLISH));

    /** {event.resource : (event.transferredBytes, timestamp)} */
    private Map<TransferResource, Pair<Long, Long>> downloads = new ConcurrentHashMap<>();

    @Override
    public void transferInitiated(TransferEvent event) {
        String message = event.getRequestType() == TransferEvent.RequestType.PUT ? "uploading "
                : "downloading ";
        LOG.trace(message + shortName(event.getResource()) + " from "
                + event.getResource().getRepositoryUrl());
    }

    @Override
    public void transferProgressed(TransferEvent event) {
        TransferResource resource = event.getResource();
        final long complete = event.getTransferredBytes();
        final long total = resource.getContentLength();
        final int percentageComplete = (int) (100f * complete / total);
        if (percentageComplete != 100) { // no progress logged if 100%

            long now = System.currentTimeMillis();
            if (downloads.containsKey(resource)) {
                Pair<Long, Long> lastLog = downloads.get(resource);
                long lastTimestamp = lastLog.getRight();
                if (lastTimestamp < (now - ONE_SECONDS)) { // old enough ->
                                                           // update
                    LOG.info("downloading " + shortName(resource) + " ("
                            + getStatus(complete, total) + ")");
                    downloads.put(resource, Pair.of(complete, now));
                } // else wait
            } else {
                LOG.info("downloading " + shortName(resource) + " ("
                        + getStatus(complete, total) + ") ...");
                downloads.put(resource,
                        Pair.of(complete, resource.getTransferStartTime()));
            }
        }
    }

    @Override
    public void transferSucceeded(TransferEvent event) {

        TransferResource resource = event.getResource();
        long contentLength = event.getTransferredBytes();
        if (contentLength >= 0) {
            String len = contentLength >= 1024 * 1024 ? toMB(contentLength)
                    + " MB" : contentLength >= 1024 ? toKB(contentLength)
                    + " KB" : contentLength + " B";

            String throughput = "";
            long duration = currentTimeMillis()
                    - resource.getTransferStartTime();
            if (duration > 0) {
                long bytes = contentLength - resource.getResumeOffset();
                double kbPerSec = (bytes / 1024.0) / (duration / 1000.0);
                throughput = " at " + FORMAT.format(kbPerSec) + " KB/sec";
            }
            LOG.info("downloaded! " + shortName(resource) + " (" + len
                    + throughput + ")");
        }
    }

    @Override
    public void transferFailed(TransferEvent event) {
        if (!(event.getException() instanceof MetadataNotFoundException)) {
            // log at trace-level, since it might only be one of the repo to be
            // downloaded from. real transfer failure handled in PipelineLoader
            LOG.trace("transferFailed:: " + event.getException().getMessage());
        }
    }

    @Override
    public void transferCorrupted(TransferEvent event) {
        LOG.warn("transfer corrupted:: " + event.getException().getMessage());
    }

    /** Creates a text message with right units and form */
    private static String getStatus(long complete, long total) {

        int percentageComplete = (int) (100f * complete / total);

        if (total >= 1024 * 1024) {
            return percentageComplete + "% of " + toMB(total) + " MB";
        } else if (total >= 1024) {
            return percentageComplete + "% of " + toKB(total) + " KB";
        } else if (total >= 0) {
            return percentageComplete + "% of " + total + " B";

        } else if (complete >= 1024 * 1024) { // total not known
            return toMB(complete) + " MB";
        } else if (complete >= 1024) {
            return toKB(complete) + " KB";
        } else {
            return complete + " B";
        }
    }

    /** keep only file name */
    private static String shortName(TransferResource resource) {
        String name = resource.getResourceName();
        if (name.lastIndexOf('/') > -1) {
            name = name.substring(name.lastIndexOf('/') + 1, name.length());
        }
        return name;
    }

    private static long toKB(long bytes) {
        return (bytes + 1023) / 1024;
    }

    private static long toMB(long bytes) {
        return toKB(toKB(bytes));
    }
}