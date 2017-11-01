package com.softserve.academy.spaced.repetition.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.softserve.academy.spaced.repetition.audit.Auditable;
import com.softserve.academy.spaced.repetition.audit.AuditingAction;
import com.softserve.academy.spaced.repetition.domain.*;
import com.softserve.academy.spaced.repetition.repository.AccountRepository;
import com.softserve.academy.spaced.repetition.repository.AuthorityRepository;
import com.softserve.academy.spaced.repetition.repository.RememberingLevelRepository;
import com.softserve.academy.spaced.repetition.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Date;

import static com.softserve.academy.spaced.repetition.domain.Account.CARDS_NUMBER;

@Component
public class GoogleAuthUtil {

    private final String FIRST_NAME = "given_name";
    private final String LAST_NAME = "family_name";
    private final String IMAGE = "picture";

    @Value("${app.social.google.client-id}")
    private String clientId;

    private final AccountRepository accountRepository;

    private final UserRepository userRepository;

    private final AuthorityRepository authorityRepository;

    private final RememberingLevelRepository rememberingLevelRepository;

    @Autowired
    public GoogleAuthUtil(AccountRepository accountRepository, UserRepository userRepository,
                          AuthorityRepository authorityRepository,
                          RememberingLevelRepository rememberingLevelRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.rememberingLevelRepository = rememberingLevelRepository;
    }

    public GoogleIdToken getGoogleIdToken(String idToken) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singleton(clientId)).build();
        try {
            return verifier.verify(idToken);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getEmail(GoogleIdToken googleIdToken) {
        GoogleIdToken.Payload payload = googleIdToken.getPayload();
        return payload.getEmail();
    }

    public boolean checkIfExistUser(String email) {
        Account account = accountRepository.findByEmail(email);
        return account != null;
    }

    public String getFirstName(GoogleIdToken googleIdToken) {
        GoogleIdToken.Payload payload = googleIdToken.getPayload();
        return (String) payload.get(FIRST_NAME);
    }

    public String getLastName(GoogleIdToken googleIdToken) {
        GoogleIdToken.Payload payload = googleIdToken.getPayload();
        return (String) payload.get(LAST_NAME);
    }

    @Auditable(action = AuditingAction.SIGN_UP_GOOGLE)
    public void saveNewGoogleUser(GoogleIdToken googleIdToken) {
        GoogleIdToken.Payload payload = googleIdToken.getPayload();
        User user = new User();
        Account account = new Account();
        Folder folder = new Folder();
        Person person = new Person();
        account.setEmail(payload.getEmail());
        account.setPassword("-1");
        account.setLastPasswordResetDate(new Date());
        account.setStatus(AccountStatus.ACTIVE);
        account.setAuthenticationType(AuthenticationType.GOOGLE);
        Authority authority = authorityRepository.findAuthorityByName(AuthorityName.ROLE_USER);
        account.setAuthorities(Collections.singleton(authority));
        person.setFirstName((String) payload.get(FIRST_NAME));
        person.setLastName((String) payload.get(LAST_NAME));
        person.setImage((String) payload.get(IMAGE));
        person.setTypeImage(ImageType.LINK);
        account.setLearningRegime(LearningRegime.CARDS_POSTPONING_USING_SPACED_REPETITION);
        account.setCardsNumber(CARDS_NUMBER);
        user.setAccount(account);
        user.setFolder(folder);
        user.setPerson(person);
        userRepository.save(user);
        rememberingLevelRepository.save(new RememberingLevel(1, "Teapot", 1, account));
        rememberingLevelRepository.save(new RememberingLevel(2, "Monkey", 3, account));
        rememberingLevelRepository.save(new RememberingLevel(3, "Beginner", 7, account));
        rememberingLevelRepository.save(new RememberingLevel(4, "Student", 14, account));
        rememberingLevelRepository.save(new RememberingLevel(5, "Expert", 30, account));
        rememberingLevelRepository.save(new RememberingLevel(6, "Genius", 60, account));
    }
}
