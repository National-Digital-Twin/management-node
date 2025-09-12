#!/bin/sh -e

#
# SPDX-License-Identifier: Apache-2.0
# © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally
# attributed to the Department for Business and Trade (UK) as the governing entity.
#

cd ..
sudo docker build -f docker/Dockerfile -t ndtp/management-node .
