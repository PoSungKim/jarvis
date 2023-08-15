package personal.benebean.jarvis.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.*;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import personal.benebean.jarvis.batch.domain.User;
import personal.benebean.jarvis.batch.listener.JobListener;
import personal.benebean.jarvis.batch.mapper.UserMapper;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MyJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TaskExecutor threadPoolTaskExecutor;
    private final JobListener jobListener;
    private final UserMapper userMapper;
    private final SqlSession batchSqlSession;
    private final SqlSessionFactory sqlSessionFactory;

    @Bean
    @Transactional(rollbackFor = Exception.class)
    public Job myJob() {

        return new JobBuilder("myJob")
                .repository(jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(chunkStep())
                .listener(jobListener)
                .build();
    }

    String output = "";

    @Bean
    @JobScope
    public Step chunkStep() {

        return new StepBuilder("chunkStep")
                .repository(jobRepository)
                .<String, String>chunk(2, transactionManager)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build()
                ;
    }

    @StepScope
    public ItemReader itemReader() {
        return new ListItemReader<>(List.of("1", "2", "3", "4", "5"));
    }

    @StepScope
    public ItemProcessor itemProcessor() {
        return new ItemProcessor<String, String>() {
            @Override
            public String process(String input) throws Exception {

                threadPoolTaskExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        output = input + "brian";
                    }
                });

                log.info("Thread {} : Input {}, Output {}", Thread.currentThread().getName(), input, output);

                return output;
            }
        };
    }

    @StepScope
    public ItemWriter itemWriter() {
        return new ItemWriter<String>() {
            @Override
            public void write(Chunk<? extends String> chunk) throws Exception {

                log.info("Thread {} : Writer {}", Thread.currentThread().getName(), chunk.getItems());

                TransactionStatus txStatus =
                        transactionManager.getTransaction(new DefaultTransactionDefinition());

                try {
                    List<? extends String> nameList = chunk.getItems();

                    for (String s : nameList) {
                        if (s.equals("3brian"))
                            throw new RuntimeException("3brian!!!! ROLLBACK!!!");

                        //batchSqlSession.getConnection().setAutoCommit(false);
                        //batchSqlSession.insert("personal.benebean.jarvis.batch.mapper.UserMapper.insertUser", new User(s));
                        userMapper.insertUser(new User(s));
                    }
                } catch (Exception e) {
                    transactionManager.rollback(txStatus);
                    throw e;
                }

                transactionManager.commit(txStatus);
            }
        };
    }
}
