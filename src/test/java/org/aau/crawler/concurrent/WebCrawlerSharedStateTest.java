package org.aau.crawler.concurrent;

import org.aau.crawler.error.CrawlingError;
import org.aau.crawler.result.BrokenLink;
import org.aau.crawler.result.Link;
import org.aau.crawler.result.WorkingLink;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class WebCrawlerSharedStateTest {

    @Test
    void testFieldAccessors() {
        BlockingQueue<CrawlTask> queue = new LinkedBlockingQueue<>();
        Set<Link> crawled = Collections.synchronizedSet(new HashSet<>());
        AtomicInteger active = new AtomicInteger(2);
        CountDownLatch latch = new CountDownLatch(1);
        List<CrawlingError> errors = Collections.synchronizedList(new CopyOnWriteArrayList<>());

        WebCrawlerSharedState state = new WebCrawlerSharedState(queue, crawled, active, latch, errors);

        assertSame(queue, state.urlQueue());
        assertSame(crawled, state.crawledLinks());
        assertSame(active, state.activeThreads());
        assertSame(latch, state.completionLatch());
        assertSame(errors, state.crawlingErrors());
    }

    @Test
    void testContainsCrawledUrlWhenPresent() {
        Set<Link> crawled = Collections.synchronizedSet(new HashSet<>());
        crawled.add(new WorkingLink("http://example.com/page1", 0, Set.of(), Set.of()));
        crawled.add(new BrokenLink("http://example.com/page2", 0));

        WebCrawlerSharedState state = new WebCrawlerSharedState(
                new LinkedBlockingQueue<>(),
                crawled,
                new AtomicInteger(),
                new CountDownLatch(1),
                Collections.synchronizedList(new CopyOnWriteArrayList<>())
        );

        assertTrue(state.containsCrawledUrl("http://example.com/page1"));
        assertTrue(state.containsCrawledUrl("http://example.com/page2"));
    }

    @Test
    void testContainsCrawledUrlWhenNotPresent() {
        Set<Link> crawled = Collections.synchronizedSet(new HashSet<>());
        crawled.add(new WorkingLink("http://example.com/page1", 0, Set.of(), Set.of()));

        WebCrawlerSharedState state = new WebCrawlerSharedState(
                new LinkedBlockingQueue<>(),
                crawled,
                new AtomicInteger(),
                new CountDownLatch(1),
                Collections.synchronizedList(new CopyOnWriteArrayList<>())
        );

        assertFalse(state.containsCrawledUrl("http://unknown.com"));
    }

    @Test
    void testContainsCrawledUrlEmptySet() {
        WebCrawlerSharedState state = new WebCrawlerSharedState(
                new LinkedBlockingQueue<>(),
                Collections.synchronizedSet(new HashSet<>()),
                new AtomicInteger(),
                new CountDownLatch(1),
                Collections.synchronizedList(new CopyOnWriteArrayList<>())
        );

        assertFalse(state.containsCrawledUrl("http://anything.com"));
    }

    @Test
    void hasActiveThreadsShouldReturnTrueIfThereAreActiveThreads() {
        WebCrawlerSharedState state = new WebCrawlerSharedState(
                new LinkedBlockingQueue<>(),
                Collections.synchronizedSet(new HashSet<>()),
                new AtomicInteger(1),
                new CountDownLatch(1),
                Collections.synchronizedList(new CopyOnWriteArrayList<>())
        );

        assertTrue(state.hasActiveThreads());
    }

    @Test
    void hasActiveThreadsShouldReturnFalseIfThereAreNoActiveThreads() {
        WebCrawlerSharedState state = new WebCrawlerSharedState(
                new LinkedBlockingQueue<>(),
                Collections.synchronizedSet(new HashSet<>()),
                new AtomicInteger(0),
                new CountDownLatch(1),
                Collections.synchronizedList(new CopyOnWriteArrayList<>())
        );

        assertFalse(state.hasActiveThreads());
    }

    @Test
    void addCrawledLinkShouldAddLinkToSet() {
        Set<Link> crawled = Collections.synchronizedSet(new HashSet<>());
        WebCrawlerSharedState state = new WebCrawlerSharedState(
                new LinkedBlockingQueue<>(),
                crawled,
                new AtomicInteger(),
                new CountDownLatch(1),
                Collections.synchronizedList(new CopyOnWriteArrayList<>())
        );
        Link link = new WorkingLink("http://test.com", 0, Set.of(), Set.of());
        state.addCrawledLink(link);
        assertTrue(state.crawledLinks().contains(link));
    }

    @Test
    void reportCrawlingErrorShouldAddErrorToList() {
        List<CrawlingError> errors = Collections.synchronizedList(new CopyOnWriteArrayList<>());
        WebCrawlerSharedState state = new WebCrawlerSharedState(
                new LinkedBlockingQueue<>(),
                Collections.synchronizedSet(new HashSet<>()),
                new AtomicInteger(),
                new CountDownLatch(1),
                errors
        );
        CrawlingError error = new CrawlingError("Test Error", new RuntimeException("Test"));
        state.reportCrawlingError(error);
        assertTrue(state.crawlingErrors().contains(error));
        assertEquals(1, state.crawlingErrors().size());
    }

    @Test
    void getNextTaskShouldReturnTaskFromQueue() throws InterruptedException {
        BlockingQueue<CrawlTask> queue = new LinkedBlockingQueue<>();
        CrawlTask expectedTask = new CrawlTask("http://task.com", 0);
        queue.put(expectedTask);

        WebCrawlerSharedState state = new WebCrawlerSharedState(
                queue,
                Collections.synchronizedSet(new HashSet<>()),
                new AtomicInteger(),
                new CountDownLatch(1),
                Collections.synchronizedList(new CopyOnWriteArrayList<>())
        );

        CrawlTask actualTask = state.getNextTask();
        assertEquals(expectedTask, actualTask);
        assertTrue(queue.isEmpty());
    }

    @Test
    void getNextTaskShouldReturnNullIfQueueIsEmptyAfterTimeout() throws InterruptedException {
        BlockingQueue<CrawlTask> queue = new LinkedBlockingQueue<>();
        WebCrawlerSharedState state = new WebCrawlerSharedState(
                queue,
                Collections.synchronizedSet(new HashSet<>()),
                new AtomicInteger(),
                new CountDownLatch(1),
                Collections.synchronizedList(new CopyOnWriteArrayList<>())
        );

        CrawlTask actualTask = state.getNextTask();
        assertNull(actualTask);
    }

    @Test
    void getNextTaskShouldThrowInterruptedExceptionIfInterrupted() {
        BlockingQueue<CrawlTask> queue = new LinkedBlockingQueue<>();
        WebCrawlerSharedState state = new WebCrawlerSharedState(
                queue,
                Collections.synchronizedSet(new HashSet<>()),
                new AtomicInteger(),
                new CountDownLatch(1),
                Collections.synchronizedList(new CopyOnWriteArrayList<>())
        );

        Thread.currentThread().interrupt();
        assertThrows(InterruptedException.class, () -> state.getNextTask());
        assertFalse(Thread.currentThread().isInterrupted(), "Interrupted status should be cleared after throwing InterruptedException");
    }


    @Test
    void addTaskShouldPutTaskInQueue() throws InterruptedException {
        BlockingQueue<CrawlTask> queue = new LinkedBlockingQueue<>();
        WebCrawlerSharedState state = new WebCrawlerSharedState(
                queue,
                Collections.synchronizedSet(new HashSet<>()),
                new AtomicInteger(),
                new CountDownLatch(1),
                Collections.synchronizedList(new CopyOnWriteArrayList<>())
        );
        CrawlTask task = new CrawlTask("http://newtask.com", 1);
        state.addTask(task);
        assertFalse(queue.isEmpty());
        assertEquals(task, queue.take());
    }

    @Test
    void hasNoFurtherTasksShouldReturnTrueIfQueueIsEmpty() {
        BlockingQueue<CrawlTask> queue = new LinkedBlockingQueue<>();
        WebCrawlerSharedState state = new WebCrawlerSharedState(
                queue,
                Collections.synchronizedSet(new HashSet<>()),
                new AtomicInteger(),
                new CountDownLatch(1),
                Collections.synchronizedList(new CopyOnWriteArrayList<>())
        );
        assertTrue(state.hasNoFurtherTasks());
    }

    @Test
    void hasNoFurtherTasksShouldReturnFalseIfQueueIsNotEmpty() throws InterruptedException {
        BlockingQueue<CrawlTask> queue = new LinkedBlockingQueue<>();
        queue.put(new CrawlTask("http://task.com", 0));
        WebCrawlerSharedState state = new WebCrawlerSharedState(
                queue,
                Collections.synchronizedSet(new HashSet<>()),
                new AtomicInteger(),
                new CountDownLatch(1),
                Collections.synchronizedList(new CopyOnWriteArrayList<>())
        );
        assertFalse(state.hasNoFurtherTasks());
    }

    @Test
    void incrementActiveThreadsShouldIncreaseCount() {
        AtomicInteger active = new AtomicInteger(0);
        WebCrawlerSharedState state = new WebCrawlerSharedState(
                new LinkedBlockingQueue<>(),
                Collections.synchronizedSet(new HashSet<>()),
                active,
                new CountDownLatch(1),
                Collections.synchronizedList(new CopyOnWriteArrayList<>())
        );
        state.incrementActiveThreads();
        assertEquals(1, active.get());
    }

    @Test
    void decrementActiveThreadsShouldDecreaseCount() {
        AtomicInteger active = new AtomicInteger(1);
        WebCrawlerSharedState state = new WebCrawlerSharedState(
                new LinkedBlockingQueue<>(),
                Collections.synchronizedSet(new HashSet<>()),
                active,
                new CountDownLatch(1),
                Collections.synchronizedList(new CopyOnWriteArrayList<>())
        );
        state.decrementActiveThreads();
        assertEquals(0, active.get());
    }

    @Test
    void countDownCompletionLatchShouldDecreaseCount() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        WebCrawlerSharedState state = new WebCrawlerSharedState(
                new LinkedBlockingQueue<>(),
                Collections.synchronizedSet(new HashSet<>()),
                new AtomicInteger(),
                latch,
                Collections.synchronizedList(new CopyOnWriteArrayList<>())
        );
        state.countDownCompletionLatch();
        assertTrue(latch.await(0, TimeUnit.MILLISECONDS));
        assertEquals(0, latch.getCount());
    }

    @Test
    void testContainsCrawledUrlWithMultipleThreads() throws InterruptedException {
        Set<Link> crawled = Collections.synchronizedSet(new HashSet<>());
        crawled.add(new WorkingLink("http://example.com/shared1", 0, Set.of(), Set.of()));
        crawled.add(new BrokenLink("http://example.com/shared2", 0));

        WebCrawlerSharedState state = new WebCrawlerSharedState(
                new LinkedBlockingQueue<>(),
                crawled,
                new AtomicInteger(),
                new CountDownLatch(1),
                Collections.synchronizedList(new CopyOnWriteArrayList<>())
        );

        int numThreads = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    assertTrue(state.containsCrawledUrl("http://example.com/shared1"));
                    assertTrue(state.containsCrawledUrl("http://example.com/shared2"));
                    assertFalse(state.containsCrawledUrl("http://example.com/nonexistent"));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    fail("Thread interrupted during test");
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        assertTrue(endLatch.await(5, TimeUnit.SECONDS), "Threads did not complete in time");
    }

    @Test
    void testAddCrawledLinkWithMultipleThreads() throws InterruptedException {
        Set<Link> crawled = Collections.synchronizedSet(new HashSet<>());
        WebCrawlerSharedState state = new WebCrawlerSharedState(
                new LinkedBlockingQueue<>(),
                crawled,
                new AtomicInteger(),
                new CountDownLatch(1),
                Collections.synchronizedList(new CopyOnWriteArrayList<>())
        );

        int numThreads = 5;
        int linksPerThread = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < linksPerThread; j++) {
                        state.addCrawledLink(new WorkingLink("http://multi.com/thread" + threadId + "/link" + j, 0, Set.of(), Set.of()));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    fail("Thread interrupted during test");
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        assertTrue(endLatch.await(10, TimeUnit.SECONDS), "Threads did not complete in time");

        assertEquals(numThreads * linksPerThread, state.crawledLinks().size());
        for (int i = 0; i < numThreads; i++) {
            for (int j = 0; j < linksPerThread; j++) {
                assertTrue(state.crawledLinks().contains(new WorkingLink("http://multi.com/thread" + i + "/link" + j, 0, Set.of(), Set.of())));
            }
        }
    }

    @Test
    void testReportCrawlingErrorWithMultipleThreads() throws InterruptedException {
        List<CrawlingError> errors = Collections.synchronizedList(new CopyOnWriteArrayList<>());
        WebCrawlerSharedState state = new WebCrawlerSharedState(
                new LinkedBlockingQueue<>(),
                Collections.synchronizedSet(new HashSet<>()),
                new AtomicInteger(),
                new CountDownLatch(1),
                errors
        );

        int numThreads = 5;
        int errorsPerThread = 50;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < errorsPerThread; j++) {
                        state.reportCrawlingError(new CrawlingError("Error from thread " + threadId + ", error " + j, new RuntimeException()));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    fail("Thread interrupted during test");
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        assertTrue(endLatch.await(10, TimeUnit.SECONDS), "Threads did not complete in time");

        assertEquals(numThreads * errorsPerThread, state.crawlingErrors().size());
        for (int i = 0; i < numThreads; i++) {
            for (int j = 0; j < errorsPerThread; j++) {
                String expectedErrorPart = "Error from thread " + i + ", error " + j;
                assertTrue(state.crawlingErrors().stream().anyMatch(e -> e.message().contains(expectedErrorPart)));
            }
        }
    }

    @Test
    void testGetNextTaskAndAddTaskWithMultipleThreads() throws InterruptedException {
        BlockingQueue<CrawlTask> queue = new LinkedBlockingQueue<>();
        WebCrawlerSharedState state = new WebCrawlerSharedState(
                queue,
                Collections.synchronizedSet(new HashSet<>()),
                new AtomicInteger(),
                new CountDownLatch(1),
                Collections.synchronizedList(new CopyOnWriteArrayList<>())
        );

        int numProducerThreads = 3;
        int numConsumerThreads = 2;
        int tasksPerProducer = 100;
        int totalTasks = numProducerThreads * tasksPerProducer;

        CountDownLatch producersStartLatch = new CountDownLatch(1);
        CountDownLatch producersEndLatch = new CountDownLatch(numProducerThreads);
        CountDownLatch consumersEndLatch = new CountDownLatch(totalTasks);

        for (int i = 0; i < numProducerThreads; i++) {
            final int producerId = i;
            new Thread(() -> {
                try {
                    producersStartLatch.await();
                    for (int j = 0; j < tasksPerProducer; j++) {
                        state.addTask(new CrawlTask("http://producer" + producerId + "/task" + j, 0));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    fail("Producer thread interrupted");
                } finally {
                    producersEndLatch.countDown();
                }
            }).start();
        }

        for (int i = 0; i < numConsumerThreads; i++) {
            new Thread(() -> {
                try {
                    while (consumersEndLatch.getCount() > 0) {
                        CrawlTask task = state.getNextTask();
                        if (task != null) {
                            consumersEndLatch.countDown();
                        }
                        TimeUnit.MILLISECONDS.sleep(1);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        producersStartLatch.countDown();
        assertTrue(producersEndLatch.await(10, TimeUnit.SECONDS), "Producers did not complete in time");
        assertTrue(consumersEndLatch.await(15, TimeUnit.SECONDS), "Consumers did not process all tasks in time. Remaining: " + consumersEndLatch.getCount());

        assertEquals(0, queue.size(), "Queue should be empty after all tasks are consumed");
    }

    @Test
    void testIncrementAndDecrementActiveThreadsWithMultipleThreads() throws InterruptedException {
        AtomicInteger active = new AtomicInteger(0);
        WebCrawlerSharedState state = new WebCrawlerSharedState(
                new LinkedBlockingQueue<>(),
                Collections.synchronizedSet(new HashSet<>()),
                active,
                new CountDownLatch(1),
                Collections.synchronizedList(new CopyOnWriteArrayList<>())
        );

        int numThreads = 100;
        CountDownLatch incrementLatch = new CountDownLatch(numThreads);
        CountDownLatch decrementLatch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            new Thread(() -> {
                state.incrementActiveThreads();
                incrementLatch.countDown();
            }).start();
        }
        assertTrue(incrementLatch.await(5, TimeUnit.SECONDS), "Increment threads did not complete");
        assertEquals(numThreads, active.get(), "All threads should have incremented the count");

        for (int i = 0; i < numThreads; i++) {
            new Thread(() -> {
                state.decrementActiveThreads();
                decrementLatch.countDown();
            }).start();
        }
        assertTrue(decrementLatch.await(5, TimeUnit.SECONDS), "Decrement threads did not complete");
        assertEquals(0, active.get(), "All threads should have decremented the count back to zero");
    }

    @Test
    void testCountDownCompletionLatchWithMultipleThreads() throws InterruptedException {
        int initialCount = 10;
        CountDownLatch latch = new CountDownLatch(initialCount);
        WebCrawlerSharedState state = new WebCrawlerSharedState(
                new LinkedBlockingQueue<>(),
                Collections.synchronizedSet(new HashSet<>()),
                new AtomicInteger(),
                latch,
                Collections.synchronizedList(new CopyOnWriteArrayList<>())
        );

        int numThreads = initialCount;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    state.countDownCompletionLatch();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    fail("Thread interrupted during countdown");
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        assertTrue(endLatch.await(5, TimeUnit.SECONDS), "Threads did not complete in time");

        assertEquals(0, latch.getCount(), "Latch should have counted down to zero");
        assertTrue(latch.await(0, TimeUnit.MILLISECONDS), "Latch should be triggered");
    }
}