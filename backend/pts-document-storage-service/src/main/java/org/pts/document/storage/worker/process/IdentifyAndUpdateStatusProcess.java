package org.pts.document.storage.worker.process;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pts.document.storage.model.enums.OutboxJobStatus;
import org.pts.document.storage.service.dto.UploadResult;
import org.pts.document.storage.service.outbox.JobManagerService;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component
@RequiredArgsConstructor
@Slf4j
public class IdentifyAndUpdateStatusProcess {
    private final JobManagerService jobManagerService;

    public void execute(Map<Long, List<UploadResult>> result) {
        Map<Long, OutboxJobStatus> itemsStatusMap = new java.util.HashMap<>(Collections.emptyMap());
        Map<Long, OutboxJobStatus> jobsStatusMap = new java.util.HashMap<>(Collections.emptyMap());

        result.forEach((key, value) -> {
                    value.forEach(item -> {

                        if (item.result() == null) {
                            itemsStatusMap.put(

                            )
                        }

                        if (result.result() == null) {
                            jobStatus.set(OutboxJobStatus.FAILED);
                            itemsStatusMap.put(
                                    item.getId(),
                                    OutboxJobStatus.FAILED
                            );
                        } else {
                            itemsStatusMap.put(
                                    item.getId(),
                                    OutboxJobStatus.DONE
                            );
                        }
                    });
                }
                );



        jobManagerService.updateJobAndItemStatus(
                job.getId(),
                jobStatus.get(),
                itemsStatusMap
        );
    }
}
