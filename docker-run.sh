#!/usr/bin/env bash
docker run -v "$PWD:/usr/src/app" -p 4000:4000 -it ktor-io serve $* -H 0.0.0.0
