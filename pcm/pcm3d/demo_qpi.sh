
echo "press any key to start"

read a

numactl --cpunodebind=0 --membind=1 ../memoptest 0 >> /dev/null &
sleep 1
numactl --cpunodebind=0 --membind=1 ../memoptest 0 >> /dev/null &
sleep 1
numactl --cpunodebind=0 --membind=1 ../memoptest 0 >> /dev/null &
sleep 1
numactl --cpunodebind=0 --membind=1 ../memoptest 0 >> /dev/null &
sleep 1
numactl --cpunodebind=0 --membind=1 ../memoptest 0 >> /dev/null &
sleep 1
numactl --cpunodebind=0 --membind=1 ../memoptest 0 >> /dev/null &
sleep 1
numactl --cpunodebind=0 --membind=1 ../memoptest 0 >> /dev/null &
sleep 1
numactl --cpunodebind=0 --membind=1 ../memoptest 0 >> /dev/null &


echo "Press any key"
read a

numactl --cpunodebind=1 --membind=0 ../memoptest 0 >> /dev/null &
sleep 1
numactl --cpunodebind=1 --membind=0 ../memoptest 0 >> /dev/null &
sleep 1
numactl --cpunodebind=1 --membind=0 ../memoptest 0 >> /dev/null &
sleep 1
numactl --cpunodebind=1 --membind=0 ../memoptest 0 >> /dev/null &
sleep 1
numactl --cpunodebind=1 --membind=0 ../memoptest 0 >> /dev/null &
sleep 1
numactl --cpunodebind=1 --membind=0 ../memoptest 0 >> /dev/null &
sleep 1
numactl --cpunodebind=1 --membind=0 ../memoptest 0 >> /dev/null &
sleep 1
numactl --cpunodebind=1 --membind=0 ../memoptest 0 >> /dev/null &
sleep 1


echo "press any key to exit"
read a

killall memoptest

