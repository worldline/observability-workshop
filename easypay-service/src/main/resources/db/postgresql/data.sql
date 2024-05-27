INSERT INTO pos_ref(id, pos_id, location, active) VALUES (1, 'POS-01', 'Vallauris France', true) ON CONFLICT DO NOTHING;
INSERT INTO pos_ref(id, pos_id, location, active) VALUES (2, 'POS-02', 'Blois France', true) ON CONFLICT DO NOTHING;
INSERT INTO pos_ref(id, pos_id, location, active) VALUES (3, 'POS-03', 'Paris France', false) ON CONFLICT DO NOTHING;
INSERT INTO pos_ref(id, pos_id, location, active) VALUES (4, 'POS-04', 'San Francisco US', true) ON CONFLICT DO NOTHING;

INSERT INTO card_ref(id, card_number, card_type, blacklisted) VALUES (1, '4485248221242242', 'VISA', true) ON CONFLICT DO NOTHING;
INSERT INTO card_ref(id, card_number, card_type, blacklisted) VALUES (2, '5261749597812879', 'MASTERCARD', true) ON CONFLICT DO NOTHING;
INSERT INTO card_ref(id, card_number, card_type, blacklisted) VALUES (3, '6011191990123805', 'DISCOVER', true) ON CONFLICT DO NOTHING;
INSERT INTO card_ref(id, card_number, card_type, blacklisted) VALUES (4, '343506189778618', 'AMERICAN_EXPRESS', true) ON CONFLICT DO NOTHING;
INSERT INTO card_ref(id, card_number, card_type, blacklisted) VALUES (5, '36031319313683', 'DINERS_CLUB', true) ON CONFLICT DO NOTHING;
INSERT INTO card_ref(id, card_number, card_type, blacklisted) VALUES (6, '6289193933258511', 'CHINA_UNION_PAY', true) ON CONFLICT DO NOTHING;
