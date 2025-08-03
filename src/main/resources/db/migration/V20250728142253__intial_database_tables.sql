create table organisation
(
    id     bigserial
        constraint pk_organisation
            primary key,
    name   varchar(150) not null
);



create table producer
(
    id            bigserial
        constraint pk_producer
            primary key,
    name          varchar(50)  not null,
    description   text         not null,
    org_id        bigint       not null
        constraint fk___org_id
            references organisation,
    active        boolean      not null,
    host          varchar(500) not null,
    port          numeric      not null,
    tls           boolean      not null,
    idp_client_id varchar(50)  not null
);



create table consumer
(
    id            bigserial
        constraint pk_consumer
            primary key,
    name          varchar(50) not null,
    org_id        bigint      not null
        constraint fk__org_id
            references organisation,
    idp_client_id varchar(50) not null
);



create table product
(
    id          bigserial not null
        constraint pk_3
            primary key,
    name        varchar(50)                                                                not null,
    topic       varchar(150)                                                               not null,
    producer_id bigint                                                                     not null
        constraint fk_2
            references producer
);



create table product_consumer
(
    product_id  bigint    not null
        constraint fk_organisation_data_provider__organisation_data_provider_id
            references product,
    consumer_id bigint    not null
        constraint fk_organisation_consumer__organisation_consumer_id
            references consumer,
    granted_ts  timestamp not null,
    validity    numeric   not null,
    constraint pk_consumer_allowed_data_provider
        primary key (product_id, consumer_id)
);

