package com.softserve.academy.spaced.repetition.controller;

import com.softserve.academy.spaced.repetition.DTO.DTOBuilder;
import com.softserve.academy.spaced.repetition.DTO.impl.DeckOfUserManagedByAdminDTO;
import com.softserve.academy.spaced.repetition.DTO.impl.UserManagedByAdminDTO;
import com.softserve.academy.spaced.repetition.audit.Auditable;
import com.softserve.academy.spaced.repetition.audit.AuditingActionType;
import com.softserve.academy.spaced.repetition.domain.Deck;
import com.softserve.academy.spaced.repetition.domain.User;
import com.softserve.academy.spaced.repetition.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
public class ManageUserController {

    @Autowired
    private UserService userService;

    @Auditable(actionType = AuditingActionType.VIEW_ALL_USERS)
    @GetMapping("/api/admin/users")
    public ResponseEntity<List<UserManagedByAdminDTO>> getAllUsers() {
        List<User> userList = userService.getAllUsers();
        Link link = linkTo(methodOn(ManageUserController.class).getAllUsers()).withSelfRel();
        List<UserManagedByAdminDTO> usersDTOList = DTOBuilder.buildDtoListForCollection(userList,
                UserManagedByAdminDTO.class, link);
        return new ResponseEntity<>(usersDTOList, HttpStatus.OK);
    }

    @Auditable(actionType = AuditingActionType.VIEW_ONE_USER)
    @GetMapping("/api/admin/users/{id}")
    public ResponseEntity<UserManagedByAdminDTO> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        Link link = linkTo(methodOn(ManageUserController.class).getUserById(id)).withSelfRel();
        UserManagedByAdminDTO userDTO = DTOBuilder.buildDtoForEntity(user, UserManagedByAdminDTO.class, link);
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    @Auditable(actionType = AuditingActionType.SET_ACCOUNT_BLOCKED)
    @PutMapping("/api/admin/users/{id}")
    public ResponseEntity<UserManagedByAdminDTO> setUsersStatusBlocked(@PathVariable Long id) {
        User userWithChangedStatus = userService.setUsersStatusBlocked(id);
        Link link = linkTo(methodOn(ManageUserController.class).setUsersStatusBlocked(id)).withSelfRel();
        UserManagedByAdminDTO userManagedByAdminDTO = DTOBuilder.buildDtoForEntity(userWithChangedStatus, UserManagedByAdminDTO.class, link);
        return new ResponseEntity<>(userManagedByAdminDTO, HttpStatus.OK);
    }

    @Auditable(actionType = AuditingActionType.SET_ACCOUNT_DELETED)
    @DeleteMapping("/api/admin/users/{id}")
    public ResponseEntity<UserManagedByAdminDTO> setUsersStatusDeleted(@PathVariable Long id) {
        User userWithChangedStatus = userService.setUsersStatusDeleted(id);
        Link link = linkTo(methodOn(ManageUserController.class).setUsersStatusDeleted(id)).withSelfRel();
        UserManagedByAdminDTO userManagedByAdminDTO = DTOBuilder.buildDtoForEntity(userWithChangedStatus, UserManagedByAdminDTO.class, link);
        return new ResponseEntity<>(userManagedByAdminDTO, HttpStatus.OK);
    }

    @Auditable(actionType = AuditingActionType.SET_ACCOUNT_ACTIVE)
    @PostMapping("/api/admin/users/{id}")
    public ResponseEntity<UserManagedByAdminDTO> setUsersStatusActive(@PathVariable Long id) {
        User userWithChangedStatus = userService.setUsersStatusActive(id);
        Link link = linkTo(methodOn(ManageUserController.class).setUsersStatusActive(id)).withSelfRel();
        UserManagedByAdminDTO userManagedByAdminDTO = DTOBuilder.buildDtoForEntity(userWithChangedStatus, UserManagedByAdminDTO.class, link);
        return new ResponseEntity<>(userManagedByAdminDTO, HttpStatus.OK);
    }

    @Auditable(actionType = AuditingActionType.ADD_DECK_TO_USER_FOLDER)
    @PostMapping("/api/admin/users/{userId}/deck/{deckId}")
    public ResponseEntity<UserManagedByAdminDTO> addExistingDeckToUsersFolder(@PathVariable("userId") Long userId, @PathVariable("deckId") Long deckId) {
        User user = userService.addExistingDeckToUsersFolder(userId, deckId);
        if (user != null) {
            Link link = linkTo(methodOn(ManageUserController.class).addExistingDeckToUsersFolder(userId, deckId)).withSelfRel();
            UserManagedByAdminDTO userManagedByAdminDTO = DTOBuilder.buildDtoForEntity(user, UserManagedByAdminDTO.class, link);
            return new ResponseEntity<UserManagedByAdminDTO>(userManagedByAdminDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Auditable(actionType = AuditingActionType.REMOVE_DECK_FROM_USER_FOLDER)
    @DeleteMapping("/api/admin/users/{userId}/deck/{deckId}")
    public ResponseEntity<UserManagedByAdminDTO> removeDeckFromUsersFolder(@PathVariable("userId") Long userId, @PathVariable("deckId") Long deckId) {
        User user = userService.removeDeckFromUsersFolder(userId, deckId);
        if (user != null) {
            Link link = linkTo(methodOn(ManageUserController.class).getUserById(userId)).withSelfRel();
            UserManagedByAdminDTO userManagedByAdminDTO = DTOBuilder.buildDtoForEntity(user, UserManagedByAdminDTO.class, link);
            return new ResponseEntity<UserManagedByAdminDTO>(userManagedByAdminDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Auditable(actionType = AuditingActionType.VIEW_DECKS_FROM_USER_FOLDER)
    @GetMapping("/api/admin/users/{userId}/decks")
    public ResponseEntity<List<DeckOfUserManagedByAdminDTO>> getAllDecksFromUsersFolder(@PathVariable("userId") Long userId) {
        List<Deck> decksFromUsersFolder = userService.getAllDecksFromUsersFolder(userId);
        Link link = linkTo(methodOn(ManageUserController.class).getAllDecksFromUsersFolder(userId)).withSelfRel();
        List<DeckOfUserManagedByAdminDTO> decksFromUsersFolderDTO = DTOBuilder.buildDtoListForCollection(decksFromUsersFolder, DeckOfUserManagedByAdminDTO.class, link);
        return new ResponseEntity<List<DeckOfUserManagedByAdminDTO>>(decksFromUsersFolderDTO, HttpStatus.OK);
    }


}
