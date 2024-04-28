/*
we will satisfy 3nf

user : user_ID,pwd,monee
item : item_ID, category, description, condition, seller ID, buy-it-now-price, date posted, status(selled,selling,expired)
bid  : item_ID, bid_price, bidder, date_posted, bid_closing_date
billing : sold item_ID, purchase date, seller_ID, buyer_ID, amount due buyers need to pay, amount of money sellers need to get paid.
i think billing is optional......
*/
CREATE TABLE user_info (
    user_id varchar(64),
    password varchar(64),
    money int,
    primary key(user_id)
);
-- User is very simple. 

CREATE TABLE item_info (
    item_id varchar(64),
    category varchar(64),
    description varchar(64),
    condition varchar(64),
    seller_id varchar(64),
    status varchar(64),
    buy_now_price varchar(64),
    date_posted timestamp,
);
-- This might be decomposed....

CREATE TABLE bid_info (

);

CREATE TABLE billing_info (

);
