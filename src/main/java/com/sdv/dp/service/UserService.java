package com.sdv.dp.service;

import com.sdv.dp.dto.RegisterRequest;
import com.sdv.dp.model.User;

public interface UserService {
    User register(RegisterRequest req);
    User getByEmail(String email);
}
