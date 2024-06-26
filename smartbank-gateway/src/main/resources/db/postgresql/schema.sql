CREATE TABLE IF NOT EXISTS bank_author (
    amount integer, 
    authorized boolean, 
    date_time timestamp(6), 
    id uuid not null, 
    card_number varchar(255), 
    expiry_date varchar(255), 
    merchant_id varchar(255), 
    primary key (id)
);
