#!/bin/sh
echo "Launching start hook -> Setting up AWS"

CONFIG=~/.aws/config
CREDENTIALS=~/.aws/credentials

mkdir -p ~/.aws
if [ ! -f "$CONFIG" ]; then
    echo "[default]" > $CONFIG
    echo "output = json" >> $CONFIG
fi
