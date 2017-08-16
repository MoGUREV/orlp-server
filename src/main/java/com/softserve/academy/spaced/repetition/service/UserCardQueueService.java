package com.softserve.academy.spaced.repetition.service;

import com.softserve.academy.spaced.repetition.domain.UserCardQueue;
import com.softserve.academy.spaced.repetition.repository.UserCardQueueRepository;
import com.softserve.academy.spaced.repetition.security.JwtUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserCardQueueService {

    @Autowired
    UserCardQueueRepository userCardQueueRepository;

    public void addUserCardQueue(UserCardQueue userCardQueue, long cardId, long deckId) {
        JwtUser user = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = user.getUsername();
        UserCardQueue userCardQueueByAccountEmailAndCardId = userCardQueueRepository.findUserCardQueueByAccountEmailAndCardId(username, cardId);

        if (userCardQueueByAccountEmailAndCardId != null) {
            userCardQueue.setId(userCardQueueByAccountEmailAndCardId.getId());
        }
        userCardQueue.setAccountEmail(username);
        userCardQueue.setCardId(cardId);
        userCardQueue.setDeckId(deckId);
        userCardQueue.setCardDate(new Date());
        userCardQueueRepository.save(userCardQueue);
    }

    public UserCardQueue getUserCardQueueById(long id) {
        return userCardQueueRepository.findOne(id);
    }
}