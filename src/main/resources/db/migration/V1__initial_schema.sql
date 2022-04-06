CREATE TABLE IF NOT EXISTS `user`
(
    `uuid`                          VARCHAR ( 128 ) NOT NULL,
    `person_identifier`             VARCHAR ( 255 ) NOT NULL,
    `user_created_ms`               bigint(20) unsigned NOT NULL DEFAULT '0',
    `user_last_updated_ms`          bigint(20) unsigned NOT NULL DEFAULT '0',
    `active`                        tinyint(1) unsigned NOT NULL DEFAULT '1',
    `close_code`                    VARCHAR(255),
    `close_code_updated_ms`         bigint(20) unsigned NOT NULL DEFAULT '0',
    `help_desk_case_references`     VARCHAR(255),
    PRIMARY KEY (`uuid`),
    UNIQUE KEY `unique_user_person_identifer` (`person_identifier`)
);

CREATE INDEX `user_person_identifier_index` ON `user` (`person_identifier`);