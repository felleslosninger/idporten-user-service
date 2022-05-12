ALTER TABLE user
    ADD COLUMN (`previous_user` VARCHAR(36));

ALTER TABLE user
    ADD CONSTRAINT user_fk_previous_user FOREIGN KEY (`previous_user`) REFERENCES user(`uuid`);
