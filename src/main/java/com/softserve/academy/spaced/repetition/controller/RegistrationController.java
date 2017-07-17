package com.softserve.academy.spaced.repetition.controller;


import com.softserve.academy.spaced.repetition.domain.User;
import com.softserve.academy.spaced.repetition.repository.UserRepository;
import com.softserve.academy.spaced.repetition.service.AccountVerificationByEmailService;
import com.softserve.academy.spaced.repetition.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@CrossOrigin
@RestController
@RequestMapping(value = "/api")
public class RegistrationController {
    @Autowired
    private RegistrationService registrationService;

    @Autowired
    UserRepository userRepository;
    @Autowired
    AccountVerificationByEmailService verificationService;

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public ResponseEntity <?> addUser(@RequestBody User userFromClient, HttpServletRequest request) {
        return registrationService.registerNewUser(userFromClient, request.getContextPath());
    }

    @RequestMapping(value = "/registrationConfirm", method = RequestMethod.GET)
    public ResponseEntity confirmRegistration
            (@RequestParam("token") String token) {
        return verificationService.accountVerification(token);
    }

    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public String test() {
        registrationService.sendConfirmationEmailMessage("http://localhost:8080/api", userRepository.findUserByAccount_Email("zadorovskyi@hotmail.com"));
        return "page";
    }
}