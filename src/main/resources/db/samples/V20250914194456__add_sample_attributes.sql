/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */


insert
into product_consumer_attribute (name, type, value, product_consumer_id)
values ('nationality', 'string', 'GBR', (select id as pid
                                         from product_consumer
                                         where product_id =
                                               (Select id from product where name = 'BrownfieldLandAvailability')
                                           and consumer_id =
                                               (Select id from consumer where idp_client_id = 'FEDERATOR_ENV')));

insert
into product_consumer_attribute (name, type, value, product_consumer_id)
values ('clearance', 'string', '0', (select id as pid
                                     from product_consumer
                                     where
                                         product_id = (Select id from product where name = 'BrownfieldLandAvailability')
                                       and consumer_id =
                                           (Select id from consumer where idp_client_id = 'FEDERATOR_ENV')));



insert
into product_consumer_attribute (name, type, value, product_consumer_id)
values ('organisation_type', 'string', 'NON-GOV3', (select id as pid
                                                    from product_consumer
                                                    where product_id =
                                                          (Select id from product where name = 'BrownfieldLandAvailability')
                                                      and consumer_id =
                                                          (Select id from consumer where idp_client_id = 'FEDERATOR_ENV')));

