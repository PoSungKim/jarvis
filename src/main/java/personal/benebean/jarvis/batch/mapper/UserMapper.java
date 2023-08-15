package personal.benebean.jarvis.batch.mapper;

import org.apache.ibatis.annotations.Mapper;
import personal.benebean.jarvis.batch.domain.User;

import java.util.List;

@Mapper
public interface UserMapper {
    List<User> getUsers();

    User getUser(String userId);

    int insertUser(User user);
}