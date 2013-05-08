#!/bin/bash

PATH=/home/torgin/dev/gitthings/PurpleMow/arduino/PurpleMow
set -x

$PATH/mowcli $*

echo $* >> /tmp/log
