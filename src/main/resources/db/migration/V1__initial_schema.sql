CREATE TABLE IF NOT EXISTS `idporten_user`
(
    `uuid`              VARCHAR ( 128 ) NOT NULL,
    `person_identifier` VARCHAR ( 255 ) NOT NULL,
    PRIMARY KEY ( `uuid` )
);