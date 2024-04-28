/*
user : user_ID,pwd,monee
item : item_ID, category, description, condition, seller ID, buy-it-now-price, date posted, status(selled,selling,expired)
bid  : item_ID, bid_price, bidder, date_posted, bid_closing_date
billing : sold item_ID, purchase date, seller_ID, buyer_ID, amount due buyers need to pay, amount of money sellers need to get paid.
i think billing is optional......
*/
CREATE TABLE user (
    user_id varchar(32),
    password varchar(32),
    money int,
    primary key (user_id)
);

