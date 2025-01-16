package com.worldline.easypay.payment.control;

import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

import com.worldline.easypay.cardref.control.CardType;
import com.worldline.easypay.cardref.entity.CardRef;
import com.worldline.easypay.cardref.entity.CardRefRepository;

@Component
public class CardValidator {

    private static final Logger LOG = LoggerFactory.getLogger(CardValidator.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM/yy")
            .withZone(ZoneId.of("UTC"));

    CardRefRepository cardRepository;

    public CardValidator(CardRefRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public boolean checkExpiryDate(String expiryDate) {

        try {
            YearMonth expiration = YearMonth.parse(expiryDate, DATE_TIME_FORMATTER);
            YearMonth today = YearMonth.now();
            boolean rc = expiration.equals(today) || expiration.isAfter(today);
            if (!rc) {
                LOG.warn("Check card expiry date does not pass: outdated: {}", expiryDate);
            }
            return rc;
        } catch (DateTimeParseException e) {
            LOG.warn("Check card expiry date does not pass: bad format: {}", expiryDate);
            return false;
        }

    }

    private static boolean checkLuhnKey(String cardNumber) {
        int[] ints = new int[cardNumber.length()];
        for (int i = 0; i < cardNumber.length(); i++) {
            ints[i] = Integer.parseInt(cardNumber.substring(i, i + 1));
        }
        for (int i = ints.length - 2; i >= 0; i = i - 2) {
            int j = ints[i];
            j = j * 2;
            if (j > 9) {
                j = j % 10 + 1;
            }
            ints[i] = j;
        }
        int sum = 0;
        for (int i = 0; i < ints.length; i++) {
            sum += ints[i];
        }

        if (sum % 10 != 0) {
            LOG.warn("Check card number Luhn key does not pass");
            return false;
        }

        return true;
    }

    public boolean isBlackListed(String cardNumber) {

        CardRef probe = new CardRef();
        probe.cardNumber = cardNumber;
        probe.blackListed = true;
        long count = cardRepository.count(Example.of(probe));

        if (count != 0) {
            LOG.warn("Check card number validity does not pass: Card is black listed");
            return true;
        }

        return false;
    }

    public boolean checkCardNumber(String cardNumber) {

        // Check card format
        String card = cardNumber.replaceAll("[^0-9]+", ""); //* remove all non-numerics
        if ((card == null) || (card.length() < 13) || (card.length() > 19)) {
            LOG.warn("Check card number format: invalid card format");
            return false;
        }

        //* Check Luhn key
        return checkLuhnKey(card);
    }

    public CardType checkCardType(String cardNumber) {
        return CardType.detect(cardNumber);
    }

}
