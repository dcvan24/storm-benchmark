#!/bin/bash 

iface=$1
if [ -z "$iface" ];then 
    echo "<Usage> ./shaper_init.sh <interface>"
    exit 1
fi
tc qdisc del dev ${iface} root
