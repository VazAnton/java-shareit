DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS item_requests;
DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS users
(
user_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
name varchar(30) NOT NULL,
email varchar(50) NOT NULL,
CONSTRAINT email_is_unique UNIQUE(email)
);

CREATE TABLE IF NOT EXISTS item_requests
(
request_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
description varchar NOT NULL,
user_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,
created TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS items
(
item_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
name varchar(30) NOT NULL,
description varchar NOT NULL,
user_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,
available BOOLEAN NOT NULL,
request_id BIGINT REFERENCES item_requests(request_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS comments
(
comment_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
text varchar NOT NULL,
item_id BIGINT REFERENCES items(item_id) ON DELETE CASCADE,
user_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,
created TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS bookings
(
booking_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
start_of_booking TIMESTAMP WITHOUT TIME ZONE NOT NULL,
end_of_booking TIMESTAMP WITHOUT TIME ZONE NOT NULL,
item_id BIGINT REFERENCES items(item_id) ON DELETE CASCADE,
user_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,
status varchar(15) NOT NULL
);