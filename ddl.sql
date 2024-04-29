/*
we will satisfy 3NF

user : user_ID,pwd,monee
item : item_ID, category, description, condition, seller ID, buy-it-now-price, date posted, status(selled,selling,expired)
bid  : item_ID, bid_price, bidder, date_posted, bid_closing_date
billing : sold item_ID, purchase date, seller_ID, buyer_ID, amount due buyers need to pay, amount of money sellers need to get paid.
i think billing is optional......
*/
CREATE TABLE user_info (
    user_id varchar(32),
    password varchar(32),
    role varchar(16),
    primary key(user_id),
    check(role in ('Admin','User'))
);
-- User is very simple. 
/*
CREATE TABLE item_info (
    item_id varchar(32),
    category varchar(32),
    description varchar(32),
    condition varchar(32),
    seller_id varchar(32),
    status varchar(64),
    buy_now_price varchar(64),
    date_posted timestamp,
);
-- This might be decomposed....

CREATE TABLE bid_info (
    item_id varchar(64),

    foreign key (item_id) references item_info on delete cascade on update cascade 
);

CREATE TABLE billing_info (

);
*/