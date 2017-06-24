#!/usr/bin/env bash
docker run -v "$PWD:/src" -p 4000:4000 mydocs serve -H 0.0.0.0
