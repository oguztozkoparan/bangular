package com.bangular.main.services;

import com.bangular.main.entity.DTO.UserDTO;
import com.bangular.main.entity.Role;
import com.bangular.main.entity.User;

import java.util.List;

public interface UserService {

    //TODO: Change all this to DTO.
    User saveUser(UserDTO user);

    Role saveRole(Role role);

    void addRoleToUser(String username, String roleName);

    User getUser(String username);

    List<User> getUsers();

    boolean checkIfUserExist(String email);

}
