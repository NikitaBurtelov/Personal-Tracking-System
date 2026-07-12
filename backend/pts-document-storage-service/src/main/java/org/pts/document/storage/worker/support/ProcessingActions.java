package org.pts.document.storage.worker.support;

import org.pts.document.storage.model.enums.ProcessingStatus;
import org.pts.document.storage.service.dto.BatchContext;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class ProcessingActions {

    public static <T> T executeAndRegroup(
            Map<ProcessingStatus, List<BatchContext>> batches,
            Supplier<T> action
    ) {
        T result = action.get();

        regroup(batches);

        return result;
    }

    public static <T> T execute(
            Supplier<T> action
    ) {

        return action.get();
    }

    public static void executeAndRegroup(
            Map<ProcessingStatus, List<BatchContext>> batches,
            Runnable action
    ) {
        action.run();

        regroup(batches);
    }

    public static void execute(
            Runnable action
    ) {
        action.run();
    }

    private static void regroup(
            Map<ProcessingStatus, List<BatchContext>> batchesGroupedByStatus
    ) {
        batchesGroupedByStatus.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(
                        BatchContext::getProcessingStatus,
                        () -> new EnumMap<>(ProcessingStatus.class),
                        Collectors.toList()
                ));
    }
}
