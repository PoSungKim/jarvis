package personal.benebean.jarvis.batch.domain;

import lombok.*;

@Data
@ToString
public class User {

    private Long id;

    private String name;

    public User(String name) {
        this.name = name;
    }
}