package org.example.relief.service.impl;

import org.example.relief.model.Role;
import org.example.relief.model.User;
import org.example.relief.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(input)
                .or(() -> userRepository.findUserByUsername(input))
                .or(() -> userRepository.findByContact(input))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(input)
                .password(user.getPassword())
                .roles(user.getRoles()
                        .stream()
                        .map(Role::getName)
                        .toArray(String[]::new))
                .build();

    }
}
