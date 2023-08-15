package personal.benebean.jarvis.batch.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import personal.benebean.jarvis.batch.domain.User;
import personal.benebean.jarvis.batch.mapper.UserMapper;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JobListener implements JobExecutionListener {

    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private final UserMapper userMapper;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        System.out.println("beforeJob");

        List<User> userList = userMapper.getUsers();
        System.out.println(userList);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        System.out.println("afterJob");

        List<User> userList = userMapper.getUsers();
        System.out.println(userList);

        threadPoolTaskExecutor.shutdown();
    }
}
