#!/bin/sh -e

cd ..
sudo docker build -f docker/Dockerfile -t ndtp/management-node .
