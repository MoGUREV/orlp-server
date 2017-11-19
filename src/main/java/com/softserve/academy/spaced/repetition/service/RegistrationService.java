package com.softserve.academy.spaced.repetition.service;

import com.softserve.academy.spaced.repetition.domain.*;
import com.softserve.academy.spaced.repetition.repository.AuthorityRepository;
import com.softserve.academy.spaced.repetition.service.validators.NullFieldsValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class RegistrationService {

    private AuthorityRepository authorityRepository;
    private UserService userService;
    private PasswordEncoder passwordEncoder;
    private MailService mailService;
    private NullFieldsValidator nullFieldsValidator;

    private AccountService accountService;

    public User registerNewUser(User user) {
        nullFieldsValidator.validate(user);
        return createNewUser(user);
    }

    public User createNewUser(User user) {
        Account account = user.getAccount();
        userService.initializeNewUser(account, account.getEmail().toLowerCase(), AccountStatus.INACTIVE,
                AuthenticationType.LOCAL);
        user.getPerson().setTypeImage(ImageType.NONE);
        user.setFolder(new Folder());
        userService.addUser(user);
        accountService.initializeLearningRegimeSettingsForAccount(account);
        return user;
    }

    public void sendConfirmationEmailMessage(User user) throws MailException {
        mailService.sendConfirmationMail(user);
    }

    @Autowired
    public void setAuthorityRepository(AuthorityRepository authorityRepository) {
        this.authorityRepository = authorityRepository;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }


    @Autowired
    public void setNullFieldsValidator(NullFieldsValidator nullFieldsValidator) {
        this.nullFieldsValidator = nullFieldsValidator;
    }

    @Autowired
    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }
}
