package com.emmjay.user.service.controller;

import com.emmjay.user.service.entities.User;
import com.emmjay.user.service.services.UserServices;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserServices userService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // CREATE USER
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    // GET SINGLE USER
    @GetMapping("/{userId}")
    //@CircuitBreaker(name="ratingHotelBreaker",fallbackMethod = "ratingHotelFallback")
//    @Retry(name="ratingHotelBreaker",fallbackMethod="ratingHotelFallback")
    @RateLimiter(name = "userRateLimiter", fallbackMethod = "ratingHotelFallback")
    public ResponseEntity<User> getSingleUser(@PathVariable String userId) {
        logger.info("Get Single User Handler: UserController");
        User user = userService.getUser(userId);
        return ResponseEntity.ok(user);
    }

    //creating fall back method for cicuit breaker
    int retryCount=1;
    public ResponseEntity<User> ratingHotelFallback(String userId,Exception ex) {
        logger.info("Fallback is executed because service is done: ",ex.getMessage());
        logger.info("Retry count: {}",retryCount);
        retryCount++;
        User user = User.builder()
                .email("dummy@email.com")
                .name("Dummy")
                .about("This user is created dummy because some service is done")
                .userId("123456")
                .build();
        return new ResponseEntity<>(user,HttpStatus.OK);
    }

    // GET ALL USERS
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> allUsers = userService.getAllUsers();
        return ResponseEntity.ok(allUsers);
    }
}