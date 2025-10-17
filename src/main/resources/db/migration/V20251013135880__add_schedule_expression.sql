/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

-- Add foreign key column to product referencing product_type(id)
alter table product_consumer add column if not exists schedule_type varchar(100);
alter table product_consumer add column if not exists schedule_expression varchar(255);
alter table product_consumer add column if not exists destination varchar(500);


alter table consumer add column if not exists schedule_type varchar(100);
alter table consumer add column if not exists schedule_expression varchar(255);

-- add source to product
alter table product add column if not exists source varchar(500);

-- schedule_type: cron, interval
update consumer set schedule_type = 'cron', schedule_expression='*/5 * * * *' where 1=1;
update product_consumer set schedule_type = 'cron', schedule_expression='*/5 * * * *' where 1=1;
