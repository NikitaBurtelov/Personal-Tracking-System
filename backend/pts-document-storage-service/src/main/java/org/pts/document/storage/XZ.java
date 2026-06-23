//@Scheduled(fixedDelay = 3000)
//public void processJobs() {
//
//    List<OutboxJob> jobs =
//            jobRepository.findByStatus("NEW");
//
//    for (OutboxJob job : jobs) {
//
//        try {
//            markProcessing(job);
//
//            List<OutboxJobItem> items =
//                    itemRepository.findByJobId(job.getId());
//
//            processItems(items);
//
//            markDone(job);
//
//        } catch (Exception e) {
//            markFailed(job);
//        }
//    }
//}
//
//public void processItems(List<OutboxJobItem> items) {
//
//    List<CompletableFuture<Void>> futures =
//            items.stream()
//                    .map(item ->
//                            CompletableFuture.runAsync(() -> {
//
//                                processDocument(item.getDocumentId());
//
//                                item.setStatus("DONE");
//
//                            }, executorService)
//                    )
//                    .toList();
//
//    CompletableFuture.allOf(
//            futures.toArray(new CompletableFuture[0])
//    ).join();
//}