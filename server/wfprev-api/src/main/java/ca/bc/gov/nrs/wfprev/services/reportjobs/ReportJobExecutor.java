package ca.bc.gov.nrs.wfprev.services.reportjobs;

import jakarta.annotation.PreDestroy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/**
 * The small thread pool report export jobs run on. Each running XLSX job holds a thread while it
 * waits for the Lambda.
 *
 * <p>The pool is wrapped rather than exposed as a Spring bean: an {@link Executor} bean would
 * replace Spring Boot's default task executor, which MVC uses for streaming responses.
 */
@Component
public class ReportJobExecutor {

    private final ThreadPoolTaskExecutor pool;

    public ReportJobExecutor(ReportJobProperties properties) {
        pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(properties.getThreads());
        pool.setMaxPoolSize(properties.getThreads());
        pool.setQueueCapacity(properties.getQueueCapacity());
        pool.setThreadNamePrefix("report-job-");
        pool.setWaitForTasksToCompleteOnShutdown(false);
        pool.initialize();
    }

    /** @throws RejectedExecutionException when the queue is full */
    public void execute(Runnable task) {
        pool.execute(task);
    }

    @PreDestroy
    void shutdown() {
        pool.shutdown();
    }
}
