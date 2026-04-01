#!/usr/bin/env bash
set -euo pipefail

count="${1:-500}"
batch_size=200

if ! [[ "$count" =~ ^[0-9]+$ ]]; then
  echo "Usage: $0 <count: 300-2000>" >&2
  exit 1
fi

if (( count < 300 || count > 2000 )); then
  echo "count must be between 300 and 2000" >&2
  exit 1
fi

roads=("东京大道" "复兴大道" "黄河大街" "金明大道" "郑开大道" "大梁路" "汉兴路" "夷山大街")
areas=("410202" "410203" "410204" "410205" "410211" "410212")
types=("追尾" "剐蹭" "刮擦" "碰撞" "侧面碰撞" "行人事故" "侧翻")
descs=("夜间行车视距不足" "早高峰车流密集" "路口变道冲突" "路面湿滑导致制动距离增加" "施工路段通行受限" "信号配时冲突")

rand_index() {
  local max="$1"
  echo $(( RANDOM % max ))
}

rand_decimal() {
  local min="$1"
  local max="$2"
  local r1="$RANDOM"
  local r2="$RANDOM"
  awk -v min="$min" -v max="$max" -v r1="$r1" -v r2="$r2" \
    'BEGIN { ratio=((r1*32768)+r2)/(32767*32768); printf "%.6f", min + ratio*(max-min) }'
}

echo "-- Auto generated mock accident data"
echo "-- Generated at: $(date '+%Y-%m-%d %H:%M:%S')"
echo "-- Row count: ${count}"
echo
echo "START TRANSACTION;"
echo

rows=()
for ((i = 1; i <= count; i++)); do
  offset=$(( (((RANDOM << 15) | RANDOM)) % 2592000 ))
  occur_time="$(date -d "-${offset} seconds" '+%Y-%m-%d %H:%M:%S')"
  road="${roads[$(rand_index "${#roads[@]}")]}"
  area="${areas[$(rand_index "${#areas[@]}")]}"
  typ="${types[$(rand_index "${#types[@]}")]}"
  desc="${descs[$(rand_index "${#descs[@]}")]}"
  lng="$(rand_decimal 114.180000 114.420000)"
  lat="$(rand_decimal 34.730000 34.860000)"
  vehicle_count=$(( RANDOM % 4 + 1 ))
  casualty_seed=$(( RANDOM % 100 ))
  if (( casualty_seed < 75 )); then
    casualty_count=0
  elif (( casualty_seed < 95 )); then
    casualty_count=1
  else
    casualty_count=2
  fi

  rows+=("('${occur_time}', '${road}', '${area}', ${lng}, ${lat}, '${typ}', ${vehicle_count}, ${casualty_count}, '${desc}', 1, NOW(), NOW(), 0)")

  if (( i % batch_size == 0 || i == count )); then
    echo "INSERT INTO accident_record"
    echo "(occur_time, road_name, area_code, longitude, latitude, accident_type, vehicle_count, casualty_count, description, created_by, created_at, updated_at, deleted)"
    echo "VALUES"
    for ((j = 0; j < ${#rows[@]}; j++)); do
      if (( j < ${#rows[@]} - 1 )); then
        echo "${rows[$j]},"
      else
        echo "${rows[$j]};"
      fi
    done
    echo
    rows=()
  fi
done

echo "COMMIT;"
