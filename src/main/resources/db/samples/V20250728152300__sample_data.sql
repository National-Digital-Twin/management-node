/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */


-- Sample data for organisation table
INSERT INTO organisation (name)
VALUES ('Environment Agency (ENV)');
INSERT INTO organisation (name)
VALUES ('Bristol City Council (BCC)');
INSERT INTO organisation (name)
VALUES ('Homes England (HEG)');


-- Sample data for producer table
INSERT INTO producer (name, description, org_id, active, host, port, tls, idp_client_id)
VALUES ('ENV-PRODUCER-1', 'ENV Producer 1', (select id from organisation where name like '%ENV%'), true,
        'https://env.gov.uk', 443, true, 'FEDERATOR_ENV');

INSERT INTO producer (name, description, org_id, active, host, port, tls, idp_client_id)
VALUES ('HEG-PRODUCER-1', 'HEG Producer 1', (select id from organisation where name like '%HEG%'), true,
        'https://heg.gov.uk', 443, true, 'FEDERATOR_HEG');


INSERT INTO producer (name, description, org_id, active, host, port, tls, idp_client_id)
VALUES ('BCC-PRODUCER-1', 'BCC Producer 1', (select id from organisation where name like '%BCC%'), true,
        'https://heg.gov.uk', 443, true, 'FEDERATOR_BCC');


-- Sample data for consumer table


INSERT INTO consumer (name, org_id, idp_client_id)
VALUES ('ENV-CONSUMER-1', (select id from organisation where name like '%ENV%'), 'FEDERATOR_ENV');


INSERT INTO consumer (name, org_id, idp_client_id)
VALUES ('BCC-CONSUMER-1', (select id from organisation where name like '%BCC%'), 'FEDERATOR_BCC');


INSERT INTO consumer (name, org_id, idp_client_id)
VALUES ('HEG-CONSUMER-1', (select id from organisation where name like '%HEG%'), 'FEDERATOR_HEG');

-- Sample data for data_provider table
INSERT INTO product (name, topic, producer_id)
VALUES ('BrownfieldLandAvailability', 'topic.BrownfieldLandAvailability',
        (select id from producer where name like '%HEG%'));;

INSERT INTO product (name, topic, producer_id)
VALUES ('PendingPlanningApplications', 'topic.PendingPlanningApplications',
        (select id from producer where name like '%BCC%'));;

INSERT INTO product (name, topic, producer_id)
VALUES ('FloodRiskMapZones', 'topic.FloodRiskMapZones', (select id from producer where name like '%ENV%'));;



/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */



-- Sample data for consumer_provider table
INSERT INTO product_consumer (product_id, consumer_id, granted_ts, validity)
VALUES ((select id from product where name = 'FloodRiskMapZones'), (select id from consumer where name like '%BCC%'),
        '2025-07-01 00:00:00', 365);


INSERT INTO product_consumer (product_id, consumer_id, granted_ts, validity)
VALUES ((select id from product where name = 'PendingPlanningApplications'),
        (select id from consumer where name like '%HEG%'), '2025-07-01 00:00:00', 365);



INSERT INTO product_consumer (product_id, consumer_id, granted_ts, validity)
VALUES ((select id from product where name = 'BrownfieldLandAvailability'),
        (select id from consumer where name like '%ENV%'), '2025-07-01 00:00:00', 365);


