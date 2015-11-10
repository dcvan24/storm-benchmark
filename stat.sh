awk -F',' '{
  if(NR > '$2'){
     tp += $10;
     spout_tp += $12;
     lat += $13;
     max_lat += $14;
     count ++;
  }
}
END{
  print tp/count, spout_tp/count, lat/count, max_lat/count;
}
' $1
