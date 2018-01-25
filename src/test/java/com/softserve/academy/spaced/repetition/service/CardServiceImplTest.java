package com.softserve.academy.spaced.repetition.service;

import com.softserve.academy.spaced.repetition.controller.utils.dto.CardFileDTO;
import com.softserve.academy.spaced.repetition.controller.utils.dto.CardFileDTOList;
import com.softserve.academy.spaced.repetition.domain.*;
import com.softserve.academy.spaced.repetition.domain.enums.LearningRegime;
import com.softserve.academy.spaced.repetition.repository.CardRepository;
import com.softserve.academy.spaced.repetition.repository.DeckRepository;
import com.softserve.academy.spaced.repetition.service.impl.CardServiceImpl;
import com.softserve.academy.spaced.repetition.util.DomainFactory;
import com.softserve.academy.spaced.repetition.utils.exceptions.EmptyFileException;
import com.softserve.academy.spaced.repetition.utils.exceptions.NotAuthorisedUserException;
import com.softserve.academy.spaced.repetition.utils.exceptions.NotOwnerOperationException;
import com.softserve.academy.spaced.repetition.utils.exceptions.WrongFormatException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Transactional
public class CardServiceImplTest {

    private final Long USER_ID = 1L;
    private final Long DECK_ID = 1L;
    private final Long CARD_ID = 1L;
    private final Integer ACCOUNT_CARDS_NUMBER = 10;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private DeckRepository deckRepository;
    @Mock
    private UserService userService;
    @Mock
    private AccountService accountService;
    @Mock
    private UserCardQueueService userCardQueueService;
    @Mock
    private DeckService deckService;
    @Mock
    private MultipartFile cardsFile;
    @Mock
    private Yaml yaml;
    @Mock
    private OutputStream outputStream;
    @InjectMocks
    private CardServiceImpl cardService;
    private Deck deck;
    private User user;
    private Card card;

    @Before
    public void setUp() throws Exception {
        Account account = DomainFactory.createAccount(1L, null, null, null, null
                , false, null, null, LearningRegime.BAD_NORMAL_GOOD_STATUS_DEPENDING
                , ACCOUNT_CARDS_NUMBER, null);
        user = DomainFactory.createUser(USER_ID, account, new Person(), new Folder(), null);
        deck = DomainFactory.createDeck(DECK_ID, null, null, null, null,
                1.0, null, new ArrayList<>(), null, null, null, null);
        card = DomainFactory.createCard(CARD_ID, "Title", "Question", "Answer", new Deck());

        when(userService.getAuthorizedUser()).thenReturn(user);
        when(cardRepository.findOne(CARD_ID)).thenReturn(card);


    }

    @Test
    public void testGetLearningCardsWithLearningRegimeBadNormalGood() throws NotAuthorisedUserException {
        List<Card> learningCards = new ArrayList<>();
        when(accountService.getCardsNumber()).thenReturn(ACCOUNT_CARDS_NUMBER);
        when(cardRepository.cardsForLearningWithOutStatus(user.getId(), DECK_ID, ACCOUNT_CARDS_NUMBER)).thenReturn(learningCards);
        when(cardRepository.cardsQueueForLearningWithStatus(user.getId(), DECK_ID, ACCOUNT_CARDS_NUMBER - learningCards.size()))
                .thenReturn(learningCards);

        List<Card> result = cardService.getLearningCards(DECK_ID);
        verify(userService).getAuthorizedUser();
        verify(accountService).getCardsNumber();
        verify(cardRepository).cardsForLearningWithOutStatus(user.getId(), DECK_ID, ACCOUNT_CARDS_NUMBER);
        verify(cardRepository).cardsQueueForLearningWithStatus(user.getId()
                , DECK_ID, ACCOUNT_CARDS_NUMBER - learningCards.size());
        assertNotNull(result);
    }

    @Test
    public void testGetLearningCardsWithLearningRegimeCardPositionUsingSpaced() throws NotAuthorisedUserException {
        List<Card> learningCards = new ArrayList<>();
        user.getAccount().setLearningRegime(LearningRegime.CARDS_POSTPONING_USING_SPACED_REPETITION);
        when(userService.getAuthorizedUser()).thenReturn(user);
        when(accountService.getCardsNumber()).thenReturn(0);
        when(cardRepository.getCardsThatNeedRepeating(eq(DECK_ID), any(Date.class), eq(user.getId()), eq(0)))
                .thenReturn(learningCards);

        List<Card> result = cardService.getLearningCards(DECK_ID);
        verify(userService).getAuthorizedUser();
        verify(accountService).getCardsNumber();
        verify(cardRepository).getCardsThatNeedRepeating(eq(DECK_ID),
                any(Date.class), eq(user.getId()), eq(0));
        assertNotNull(result);
    }

    @Test
    public void testGetLearningCardsByNotAuthorisedUser() throws NotAuthorisedUserException {
        when(userService.getAuthorizedUser()).thenThrow(new NotAuthorisedUserException());
        when(cardRepository.findAllByDeckId(DECK_ID)).thenReturn(new ArrayList<>());
        when(accountService.getCardsNumber()).thenReturn(0);

        List<Card> result = cardService.getLearningCards(DECK_ID);
        verify(userService).getAuthorizedUser();
        verify(cardRepository).findAllByDeckId(DECK_ID);
        verify(accountService).getCardsNumber();
        assertNotNull(result);
    }

    @Test
    public void testGetCard() {
        Card result = cardService.getCard(CARD_ID);
        verify(cardRepository).findOne(CARD_ID);
        assertEquals(card, result);
    }

    @Test
    public void testAddCard() {
        when(deckRepository.findOne(DECK_ID)).thenReturn(deck);
        when(cardRepository.save(card)).thenReturn(card);

        cardService.addCard(card, DECK_ID);
        verify(deckRepository).findOne(DECK_ID);
        verify(cardRepository).save(card);
    }

    @Test
    public void testUpdateCard() {
        when(cardRepository.save(card)).thenReturn(card);

        cardService.updateCard(CARD_ID, card);
        verify(cardRepository).findOne(DECK_ID);
        verify(cardRepository).save(card);
    }

    @Test
    public void testGetCardsQueueWithOutStatus() throws NotAuthorisedUserException {
        List<Card> learningCardsTemp = new ArrayList<>();
        learningCardsTemp.add(card);
        learningCardsTemp.add(card);
        when(accountService.getCardsNumber()).thenReturn(1);
        when(cardRepository.cardsForLearningWithOutStatus(USER_ID, DECK_ID, 1))
                .thenReturn(learningCardsTemp);

        List<Card> result = cardService.getCardsQueue(DECK_ID);
        verify(userService).getAuthorizedUser();
        verify(accountService).getCardsNumber();
        verify(cardRepository).cardsForLearningWithOutStatus(USER_ID, DECK_ID, 1);
        assertEquals(learningCardsTemp, result);
    }

    @Test
    public void testGetCardsQueueWithStatus() throws NotAuthorisedUserException {
        List<Card> cardsQueue = new ArrayList<>();
        cardsQueue.add(card);
        cardsQueue.add(card);
        List<Card> cardsQueueTemp = new ArrayList<>(cardsQueue);
        cardsQueueTemp.add(card);
        when(accountService.getCardsNumber()).thenReturn(3);
        when(cardRepository.cardsForLearningWithOutStatus(USER_ID, DECK_ID, 3))
                .thenReturn(cardsQueue);
        when(cardRepository.cardsQueueForLearningWithStatus(user.getId(), DECK_ID, 3))
                .thenReturn(cardsQueueTemp);

        List<Card> result = cardService.getCardsQueue(DECK_ID);
        verify(userService).getAuthorizedUser();
        verify(accountService).getCardsNumber();
        verify(cardRepository).cardsForLearningWithOutStatus(USER_ID, DECK_ID, 3);
        verify(cardRepository).cardsQueueForLearningWithStatus(USER_ID, DECK_ID, 3);
        assertEquals(cardsQueue, result);
    }

    @Test(expected = NotAuthorisedUserException.class)
    public void testGetCardsQueueByNotAuthorisedUser() throws NotAuthorisedUserException {
        when(userService.getAuthorizedUser()).thenThrow(new NotAuthorisedUserException());

        cardService.getCardsQueue(DECK_ID);
        verify(userService).getAuthorizedUser();
    }

    @Test
    public void testDeleteCard() {
        doNothing().when(cardRepository).deleteCardById(CARD_ID);

        cardService.deleteCard(CARD_ID);
    }

    @Test
    public void testGetAdditionalLearningCards() throws NotAuthorisedUserException {
        List<Card> learningCards = new ArrayList<>();
        when(accountService.getCardsNumber()).thenReturn(ACCOUNT_CARDS_NUMBER);
        when(cardRepository.getPostponedCards(DECK_ID, new Date(), user.getId(), ACCOUNT_CARDS_NUMBER))
                .thenReturn(learningCards);

        cardService.getAdditionalLearningCards(DECK_ID);
        verify(userService).getAuthorizedUser();
        verify(accountService).getCardsNumber();
        verify(cardRepository).getPostponedCards(eq(DECK_ID), any(Date.class), eq(USER_ID), eq(ACCOUNT_CARDS_NUMBER));
    }

    @Test(expected = NotAuthorisedUserException.class)
    public void testGetAdditionalLearningCardsByNotAuthorisedUser() throws NotAuthorisedUserException {
        when(userService.getAuthorizedUser()).thenThrow(new NotAuthorisedUserException());

        cardService.getAdditionalLearningCards(DECK_ID);
        verify(userService).getAuthorizedUser();
    }

    @Test
    public void testAreThereNotPostponedCardsAvailable() throws NotAuthorisedUserException {
        List<Card> learningCards = new ArrayList<>();
        learningCards.add(card);
        when(userCardQueueService.countCardsThatNeedRepeating(DECK_ID)).thenReturn(0L);
        when(cardRepository.getNewCards(DECK_ID, user.getId(), user.getAccount().getCardsNumber()))
                .thenReturn(learningCards);

        boolean result = cardService.areThereNotPostponedCardsAvailable(DECK_ID);
        verify(userService).getAuthorizedUser();
        verify(userCardQueueService).countCardsThatNeedRepeating(DECK_ID);
        verify(cardRepository).getNewCards(DECK_ID, user.getId(), user.getAccount().getCardsNumber());
        assertEquals(true, result);
    }

    @Test(expected = NotAuthorisedUserException.class)
    public void testAreThereNotPostponedCardsAvailableByNotAuthorisedUser() throws NotAuthorisedUserException {
        when(userService.getAuthorizedUser()).thenThrow(new NotAuthorisedUserException());

        cardService.areThereNotPostponedCardsAvailable(DECK_ID);
        verify(userService).getAuthorizedUser();
    }

    @Test
    public void testUploadCards() throws WrongFormatException, EmptyFileException, NotOwnerOperationException,
            NotAuthorisedUserException, IOException {
        CardFileDTO cardFileDTO = new CardFileDTO();
        cardFileDTO.setAnswer("answer");
        cardFileDTO.setQuestion("question");
        cardFileDTO.setTitle("title");

        List<CardFileDTO> learningCards = new ArrayList<>();
        learningCards.add(cardFileDTO);

        CardFileDTOList cardFileDTOList = new CardFileDTOList();
        cardFileDTOList.setCards(learningCards);

        InputStream anyInputStream = new ByteArrayInputStream("test data".getBytes());

        when(deckService.getDeckUser(DECK_ID)).thenReturn(deck);
        when(cardsFile.getContentType()).thenReturn("application/octet-stream");
        when(cardsFile.isEmpty()).thenReturn(false);
        when(cardsFile.getInputStream()).thenReturn(anyInputStream);
        when(yaml.loadAs(anyInputStream,CardFileDTOList.class)).thenReturn(cardFileDTOList);

        cardService.uploadCards(cardsFile, DECK_ID);
        verify(deckService).getDeckUser(DECK_ID);
        verify(cardsFile).getContentType();
        verify(cardsFile).isEmpty();
        verify(cardsFile).getInputStream();
//        verify(yaml).loadAs(anyInputStream, CardFileDTOList.class);
    }

    @Test(expected = WrongFormatException.class)
    public void testUploadCardsWrongFormat() throws WrongFormatException, EmptyFileException, NotOwnerOperationException,
            NotAuthorisedUserException, IOException {
        when(deckService.getDeckUser(DECK_ID)).thenReturn(deck);
        when(cardsFile.getContentType()).thenReturn("application/octet-strea");

        cardService.uploadCards(cardsFile, DECK_ID);
        verify(deckService).getDeckUser(DECK_ID);
        verify(cardsFile).getContentType();
    }

    @Test(expected = EmptyFileException.class)
    public void testUploadCardsEmptyFile() throws WrongFormatException, EmptyFileException, NotOwnerOperationException,
            NotAuthorisedUserException, IOException {
        when(deckService.getDeckUser(DECK_ID)).thenReturn(deck);
        when(cardsFile.getContentType()).thenReturn("application/octet-stream");
        when(cardsFile.isEmpty()).thenReturn(true);

        cardService.uploadCards(cardsFile, DECK_ID);
        verify(deckService).getDeckUser(DECK_ID);
        verify(cardsFile).getContentType();
        verify(cardsFile).isEmpty();
    }

    @Test(expected = NotAuthorisedUserException.class)
    public void testUploadCardsNotAuthorisedUser() throws WrongFormatException, EmptyFileException, NotOwnerOperationException,
            NotAuthorisedUserException, IOException {
        when(deckService.getDeckUser(DECK_ID)).thenThrow(new NotAuthorisedUserException());

        cardService.uploadCards(cardsFile, DECK_ID);
        verify(deckService).getDeckUser(DECK_ID);
    }


    @Test
    public void testDownloadCards() {
        List<Card> cardList = new ArrayList<>();
        cardList.add(card);
        when(cardRepository.findAllByDeckId(DECK_ID)).thenReturn(cardList);

        cardService.downloadCards(DECK_ID, outputStream);
        verify(cardRepository).findAllByDeckId(DECK_ID);
    }

    @Test
    public void testDownloadCardsTemplate() {
        cardService.downloadCardsTemplate(outputStream);
    }
}