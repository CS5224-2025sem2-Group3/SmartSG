CREATE TABLE "User" (
    user_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    school VARCHAR(150)
);
CREATE TABLE UserProfile (
    user_id INT PRIMARY KEY,
    move_in_window VARCHAR(50), -- should be datetime
    lease_preference VARCHAR(50), -- how many month
    sleep_schedule VARCHAR(50), -- enum
    cleanliness VARCHAR(50), -- enum
    smoking BOOLEAN,
    gender VARCHAR(20), -- enum

    CONSTRAINT fk_userprofile_user
        FOREIGN KEY (user_id)
        REFERENCES "User"(user_id)
        ON DELETE CASCADE
);
CREATE TABLE Listing (
    listing_id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    address TEXT,
    rent NUMERIC(10,2),
    lease VARCHAR(50), -- how many month
    flat_type VARCHAR(50),
    available_from DATE
);

CREATE TABLE Favorites (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    listing_id INT NOT NULL,

    CONSTRAINT fk_fav_user
        FOREIGN KEY (user_id)
        REFERENCES "User"(user_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_fav_listing
        FOREIGN KEY (listing_id)
        REFERENCES Listing(listing_id)
        ON DELETE CASCADE,

    CONSTRAINT unique_favorite UNIQUE(user_id, listing_id)
);

CREATE TABLE "Group" (
    group_id SERIAL PRIMARY KEY,
    listing_id INT NOT NULL,
    status VARCHAR(50), -- recruiting, full, closed
    max_people INT,
    cur_people INT DEFAULT 0,

    CONSTRAINT fk_group_listing
        FOREIGN KEY (listing_id)
        REFERENCES Listing(listing_id)
        ON DELETE CASCADE
);

CREATE TABLE GroupMember (
    group_id INT NOT NULL,
    user_id INT NOT NULL,
    role VARCHAR(50), -- leader, member

    PRIMARY KEY (group_id, user_id),

    CONSTRAINT fk_member_group
        FOREIGN KEY (group_id)
        REFERENCES "Group"(group_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_member_user
        FOREIGN KEY (user_id)
        REFERENCES "User"(user_id)
        ON DELETE CASCADE
);

CREATE TABLE GroupRequest (
    request_id SERIAL PRIMARY KEY,
    group_id INT NOT NULL,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    status VARCHAR(50), -- pending, accepted, rejected
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_request_group
        FOREIGN KEY (group_id)
        REFERENCES "Group"(group_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_request_sender
        FOREIGN KEY (sender_id)
        REFERENCES "User"(user_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_request_receiver
        FOREIGN KEY (receiver_id)
        REFERENCES "User"(user_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_favorites_user
ON Favorites(user_id);

CREATE INDEX idx_group_listing
ON "Group"(listing_id);

CREATE INDEX idx_groupmember_user
ON GroupMember(user_id);

CREATE INDEX idx_grouprequest_receiver
ON GroupRequest(receiver_id);