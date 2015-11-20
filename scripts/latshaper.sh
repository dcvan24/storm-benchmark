iface=$1
lat=$2
dst_ip=$3
mark=${dst_ip: -1}

if [ -z "$iface" -o -z "$lat" -o -z "$dst_ip" ];then
    echo '<Usage> ./latshaper <interface> <latency(ms)> <destination IP>'
    exit 1
fi

#load module
modprobe sch_netem

tc qdisc add dev ${iface} root handle 1: prio
tc qdisc add dev ${iface} parent 1:1 handle ${mark}: netem delay ${lat}ms
tc filter add dev ${iface} parent 1:0 protocol ip pref 55 handle ::55 u32 match ip dst ${dst_ip} flowid ${mark}:1
