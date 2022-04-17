package com.bangular.main.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.bangular.main.repository.UserRepository;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;

import java.util.ArrayList;
import java.util.Arrays;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)
public class UserTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testCreateUser() {
        User user = new User();
        user.setName("Mehmet Oguz Tozkoparan");
        user.setUsername("dustbreaker");
        user.setPassword("dustbreaker");
        user.setRoles(new ArrayList<Role>(Arrays.asList()));

        User savedUser = userRepository.save(user);
        User existUser = entityManager.find(User.class, savedUser.getUserId());

        assertThat(user.getName()).isEqualTo(existUser.getName());
    }

}
