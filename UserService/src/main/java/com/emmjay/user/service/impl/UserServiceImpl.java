package com.emmjay.user.service.impl;

import com.emmjay.user.service.entities.Hotel;
import com.emmjay.user.service.entities.Rating;
import com.emmjay.user.service.entities.User;
import com.emmjay.user.service.exception.ResourceNotFoundException;
import com.emmjay.user.service.external.services.HotelServices;
import com.emmjay.user.service.repositories.UserRepository;
import com.emmjay.user.service.services.UserServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserServices {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HotelServices hotelService;

    private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public User saveUser(User user) {

        // Generate random UUID for userId
        String randomUserId = UUID.randomUUID().toString();
        user.setUserId(randomUserId); // Make sure your entity has field: private String userId;

        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUser(String userId) {
        return null;
    }


    //get Single user
    @Override
    public User getUser(User userId) {
        //get user from database with the help of user repository
        User user = userRepository.findById(String.valueOf(userId)).orElseThrow(() -> new ResourceNotFoundException("User with given id is not found on server!!"));
        //fetch rating of the above user from Rating Service
        //http://localhost:8083/rating/users/0b7fee10-11bc-425f-8855-27a178325a7a
       Rating[] ratingsOfUsers = restTemplate.getForObject("http://RATINGSERVICE/rating/users/" + userId.getUserId(), Rating[].class);
       logger.info("{}",ratingsOfUsers);

        List<Rating> ratings = Arrays.stream(ratingsOfUsers).toList();

        List<Rating> ratingList = ratings.stream().map(rating -> {
            //api call to hotel service to get the hotel
//            ResponseEntity<Hotel> forEntity = restTemplate.getForEntity("http://HOTELSERVICE/hotels/" + rating.getHotelId(), Hotel.class);

            Hotel hotel = hotelService.getHotel(rating.getHotelId());
//            logger.info("response status code: {}",forEntity.getStatusCode());
            //set the hotel to rating
            rating.setHotel(hotel);
            //return the rating
            return rating;
        }).collect(Collectors.toList());
        user.setRatings(ratingList);
        return user;
    }
}