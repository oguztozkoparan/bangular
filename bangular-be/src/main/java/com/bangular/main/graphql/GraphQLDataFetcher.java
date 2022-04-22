package com.bangular.main.graphql;

import com.bangular.main.entity.User;
import com.bangular.main.services.UserServiceImpl;
import graphql.schema.DataFetcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GraphQLDataFetcher {

    private final UserServiceImpl userService;

    public DataFetcher<List<User>> getUsers() {
        return dataFetchingEnvironment -> {
            return userService.getUsers();
        };
    }

    public DataFetcher<User> getUserByUsername() {
        return dataFetchingEnvironment -> {
            String username = dataFetchingEnvironment.getArgument("username");
            return userService.getUser(username);
        };
    }
}
