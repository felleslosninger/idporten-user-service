CREATE TABLE IF NOT EXISTS `idp_user`
(
    `uuid`                          VARCHAR (36) NOT NULL,
    `person_identifier`             VARCHAR (255) NOT NULL UNIQUE,
    `user_created_ms`               bigint(20) unsigned NOT NULL DEFAULT '0',
    `user_last_updated_ms`          bigint(20) unsigned NOT NULL DEFAULT '0',
    `active`                        tinyint(1) unsigned NOT NULL DEFAULT '1',
    `closed_code`                   VARCHAR(50),
    `closed_code_updated_ms`        bigint(20) unsigned NOT NULL DEFAULT '0',
    `help_desk_case_references`     VARCHAR(255),
    `previous_user`                 VARCHAR(36),
    PRIMARY KEY (`uuid`),
    CONSTRAINT user_fk_previous_user FOREIGN KEY (`previous_user`) REFERENCES `idp_user`(`uuid`)
);

CREATE INDEX `user_person_identifier_index` ON `idp_user` (`person_identifier`);

CREATE TABLE IF NOT EXISTS `login`
(
    `id`                           bigint(20) NOT NULL AUTO_INCREMENT,
    `user_uuid`                    VARCHAR(36) NOT NULL,
    `eid_name`                     VARCHAR ( 255 ) NOT NULL,
    `first_login_ms`               bigint(20) unsigned NOT NULL DEFAULT '0',
    `last_login_ms`                bigint(20) unsigned NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    CONSTRAINT `eid_user_fk` FOREIGN KEY (`user_uuid`) REFERENCES `idp_user` (`uuid`),
    CONSTRAINT `eid_user_uuid_name_unique` UNIQUE (`user_uuid`, `eid_name`)
);

CREATE INDEX `eid_name_user_uuid_index` ON `login` (`user_uuid`, `eid_name`) ;