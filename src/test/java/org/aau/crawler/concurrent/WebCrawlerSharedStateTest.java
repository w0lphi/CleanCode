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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}