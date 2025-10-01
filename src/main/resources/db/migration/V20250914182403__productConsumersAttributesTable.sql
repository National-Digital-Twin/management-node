/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */


-- Alter table to add a surrogate primary key column as requested
alter table product_consumer add column id bigserial;
-- Switch primary key from (product_id, consumer_id) to the new id column
alter table product_consumer drop constraint pk_consumer_allowed_data_provider;
alter table product_consumer add constraint pk_consumer_allowed_data_provider primary key (id);
-- Preserve uniqueness of the original natural key
alter table product_consumer add constraint uq_product_consumer_pair unique (product_id, consumer_id);




CREATE TABLE product_consumer_attribute
(
    "id"                bigserial NOT NULL,
    name                varchar(150) NOT NULL,
    type                varchar(50) NOT NULL,
    value               varchar(500) NOT NULL,
    product_consumer_id bigserial NOT NULL,
    CONSTRAINT PK_product_consumer_attribute_id PRIMARY KEY ( "id" ),
    CONSTRAINT FK_product_consumer_attribute__product_consumer_id FOREIGN KEY ( product_consumer_id ) REFERENCES product_consumer ( "id" )
);