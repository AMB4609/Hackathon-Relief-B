package org.example.relief.service;

import org.example.relief.model.User;

public interface LocalNotiService {
    void save(User receiver,
              String title,
              String body,
              String type,
              String referenceId);
}
