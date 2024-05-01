/*
we will satisfy 3NF

//기본적으로 날짜는 겹칠 수 있다고 생각한다.

user_info : user_id,password,role
//user_id -> password,role

item_info : item_id,seller_id,category,condition,description,bin_price,date_posted,date_expire
//item_id -> seller_id,category,condition,description,bin_price,date_posted,date_expire
//seller_id,date_posted -> item_id,category,condition,description,bin_price,date_expire


bidding_info : bid_id,item_id,buyer_id,bidding_time,price
//bid_id -> item_id,buyer_id,bidding_time,price
//buyer_id,bidding_time -> bid_id,item_id,price

billing_info : bid_id,dealed_date(BIN의 경우 bidding 만료일자와 다름)

*/

CREATE TABLE user_info (
    user_id varchar(32),
    password varchar(32),
    role varchar(32),
    primary key(user_id),
    check(role in ('Admin','User'))
);

CREATE TABLE item_info (
    item_id varchar(32),
    seller_id varchar(32),
    category varchar(32),
    condition varchar(32),
    description varchar(32),
    bin_price int,
    date_posted timestamp,
    date_expire timestamp,
    primary key(item_id),
    foreign key(seller_id) references user_info(user_id) on update cascade
);

CREATE TABLE bid_info (
    bid_id varchar(32),
    item_id varchar(32),
    buyer_id varchar(32),
    bid_posted timestamp,
    price int,
    primary key(bid_id),
    foreign key(item_id) references item_info(item_id) on update cascade on delete cascade,
    foreign key(buyer_id) references user_info(user_id) on update cascade on delete cascade
);

CREATE TABLE billing_info (
    bid_id varchar(32)
);