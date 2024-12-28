
CREATE TABLE confirmation_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expire_at TIMESTAMP NOT NULL,
    confirmed_at TIMESTAMP NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES user_registration (id)
);
