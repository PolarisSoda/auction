/*
we will satisfy 3NF
**같은 유저에 대해서, 같은 timestamp가 나올수 없다고 가정한다.**

billing_info : item_id,buyer_id,purchased_date,price
item_id,buyer_id,purchased_date,price
*/

--user_id -> password,role
CREATE TABLE user_info (
    user_id varchar(32),
    password varchar(32) not null,
    role varchar(32) not null,
    primary key(user_id)
);

--item_id -> seller_id,category,condition,description,bin_price,date_posted,date_expire
--seller_id,date_posted -> item_id,category,condition,description,bin_price,date_expire
CREATE TABLE item_info (
    item_id varchar(32),
    seller_id varchar(32) not null,
    category varchar(32) not null,
    condition varchar(32) not null,
    description varchar(32),
    bin_price int not null,
    date_posted timestamp(6) not null,
    date_expire timestamp not null,
    primary key(item_id),
    foreign key(seller_id) references user_info(user_id) on update cascade on delete cascade
);

--bid_id -> item_id,buyer_id,bidding_time,price
--buyer_id,bidding_time -> bid_id,item_id,price
CREATE TABLE bid_info (
    bid_id varchar(32),
    item_id varchar(32),
    buyer_id varchar(32),
    bid_posted timestamp(6) not null,
    price int not null,
    primary key(bid_id),
    foreign key(item_id) references item_info(item_id) on update cascade on delete cascade,
    foreign key(buyer_id) references user_info(user_id) on update cascade on delete cascade
);

--item_id -> buyer_id,purchased_date,price
--buyer_id,purchased_date -> item_id,price
CREATE TABLE billing_info (
    item_id varchar(32),
    buyer_id varchar(32),
    purchased_date timestamp(6) not null,
    price int not null,
    primary key(item_id),
    foreign key(item_id) references item_info(item_id) on update cascade on delete cascade,
    foreign key(buyer_id) references user_info(user_id) on update cascade on delete cascade
);