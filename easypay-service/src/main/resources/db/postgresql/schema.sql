CREATE TABLE IF NOT EXISTS card_ref (
    blacklisted boolean, 
    id bigserial not null, 
    card_number varchar(255) unique, 
    card_type varchar(255) check (card_type in ('UNKNOWN','VISA','MASTERCARD','AMERICAN_EXPRESS','DINERS_CLUB','DISCOVER','JCB','CHINA_UNION_PAY')), 
    primary key (id)
);

CREATE TABLE IF NOT EXISTS payment (
    amount integer, 
    authorized boolean, 
    bank_called boolean, 
    date_time timestamp(6), 
    response_time bigint, 
    authorization_id uuid, 
    id uuid not null, 
    card_number varchar(255), 
    card_type varchar(255) check (card_type in ('UNKNOWN','VISA','MASTERCARD','AMERICAN_EXPRESS','DINERS_CLUB','DISCOVER','JCB','CHINA_UNION_PAY')), 
    expiry_date varchar(255), 
    pos_id varchar(255), 
    processing_mode varchar(255) check (processing_mode in ('STANDARD','FALLBACK')), 
    response_code varchar(255) check (response_code in ('ACCEPTED','INACTIVE_POS','INVALID_CARD_NUMBER','BLACK_LISTED_CARD_NUMBER','UNKNWON_CARD_TYPE','AUTHORIZATION_DENIED','AMOUNT_EXCEEDED')), 
    primary key (id)
);

CREATE TABLE IF NOT EXISTS pos_ref (
    active boolean, 
    id bigserial not null, 
    location varchar(255), 
    pos_id varchar(255) unique, 
    primary key (id)
);