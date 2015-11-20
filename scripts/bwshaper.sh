#!/bin/bash 

iface=$1
bw=$2
dst_ip=$3

if [ -z "$iface" -o -z "$bw" -o -z "$dst_ip" ];then 
    echo '<Usage> ./bwshaper.sh <interface> <bandwidth(kbit)> <destination IP>'
    exit 1
fi

down_mark=${dst_ip: -1}
up_mark=$((down_mark + 1))

#load module
modprobe sch_htb

#init
tc qdisc add dev ${iface} root handle 1: htb default 30
tc class add dev ${iface} parent 1: classid 1:${down_mark} htb rate ${bw}
tc class add dev ${iface} parent 1: classid 1:${up_mark} htb rate ${bw}
tc filter add dev ${iface} protocol ip parent 1:0 prio 1 u32 match ip dst ${dst_ip}/32 flowid 1:${down_mark}
tc filter add dev ${iface} protocol ip parent 1:0 prio 1 u32 match ip dst ${dst_ip}/32 flowid 1:${up_mark}
