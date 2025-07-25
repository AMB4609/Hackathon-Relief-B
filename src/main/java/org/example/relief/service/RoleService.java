package org.example.relief.service;

import org.example.relief.model.Role;

import java.util.List;

public interface RoleService {
    Role createRole(Role role) throws Exception;

    List<Role> getRoles();

    Role getRoleByName(String name) throws Exception;
}
