package poly.edu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import poly.edu.dao.*;
import poly.edu.dto.DiaChiJsonDTO;
import poly.edu.entity.*;
import poly.edu.service.AuthService;
import poly.edu.service.EmailService;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employee/users")
public class QLUsersController {
    @Autowired
    private UsersDAO usersDAO;

    @Autowired
    private QuanTriDAO quanTriDAO;

    @Autowired
    private AuthService authService;

    @Autowired
    private EmailService emailService;

    private final ObjectMapper objectMapper = new ObjectMapper();
}
