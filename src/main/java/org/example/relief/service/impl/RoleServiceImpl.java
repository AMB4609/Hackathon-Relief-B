package org.example.relief.service.impl;

import org.example.relief.model.Role;
import org.example.relief.repository.RoleRepository;
import org.example.relief.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role createRole(Role role) throws Exception {
        Optional<Role> optionalRole = roleRepository.findRoleByName(role.getName());
        if(optionalRole.isPresent()){
            throw new Exception("Role Name Already Exists.");
        }else {
            return roleRepository.save(role);
        }
    }

    @Override
    public List<Role> getRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Role getRoleByName(String name) throws Exception {
        Optional<Role> optionalRole = roleRepository.findRoleByName(name);
        if(optionalRole.isPresent()){
            return optionalRole.get();
        }else {
            throw new Exception("Role Not Found.");
        }
    }
}
