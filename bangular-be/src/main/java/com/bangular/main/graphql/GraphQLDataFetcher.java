package com.bangular.main.graphql;

import com.bangular.main.entity.User;
import com.bangular.main.services.UserServiceImpl;
import graphql.schema.DataFetcher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
//@RequiredArgsConstructor
public class GraphQLDataFetcher {

    @Autowired
    private UserServiceImpl userService;

    DataFetcher<List<User>> getUsers() {
        return resolvedData -> {
            List<User> userEntities = userService.getUsers();
            return userEntities;
        };
    }

    DataFetcher<List<User>> getUsersByRoles() {
        return resolvedData -> {
            List<User> userEntities = userService.getUsers();
            if(resolvedData.getArguments().containsKey("roles")){
                List<Integer> roles = resolvedData.getArgument("roles");
                userEntities = userEntities.stream().filter(user -> roles.contains(user.getRoles())).collect(Collectors.toList());
            }
            return userEntities;
        };
    }

}
