CREATE TABLE IF NOT EXISTS `user`
(
    `uuid`                          VARCHAR ( 128 ) NOT NULL,
    `person_identifier`             VARCHAR ( 255 ) NOT NULL UNIQUE,
    `user_created_ms`               bigint(20) unsigned NOT NULL DEFAULT '0',
    `user_last_updated_ms`          bigint(20) unsigned NOT NULL DEFAULT '0',
    `active`                        tinyint(1) unsigned NOT NULL DEFAULT '1',
    `close_code`                    VARCHAR(255),
    `close_code_updated_ms`         bigint(20) unsigned NOT NULL DEFAULT '0',
    `help_desk_case_references`     VARCHAR(255),
    PRIMARY KEY (`uuid`)
);

CREATE INDEX `user_person_identifier_index` ON `user` (`person_identifier`);

CREATE TABLE IF NOT EXISTS `eid`
(
    `id`                           BIGINT(20) NOT NULL,
    `user_uuid`                    VARCHAR(128) NOT NULL,
    `name`                         VARCHAR ( 255 ) NOT NULL,
    `first_login_ms`               bigint(20) unsigned NOT NULL DEFAULT '0',
    `last_login_ms`                bigint(20) unsigned NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    CONSTRAINT `eid_user_fk` FOREIGN KEY (`user_uuid`) REFERENCES `user` (`uuid`),
    CONSTRAINT `eid_user_uuid_name_unique` UNIQUE (`user_uuid`, `name`)
);

CREATE INDEX `eid_name_user_uuid_index` ON `eid` (`user_uuid`, `name`);