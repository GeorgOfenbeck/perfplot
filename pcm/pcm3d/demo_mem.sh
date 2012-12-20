
for i in 1 2 3 4; do

sh run_mem_op_test_aff.sh 0 &
sh run_mem_op_test_aff.sh 1 &
sh run_mem_op_test_aff.sh 2 &
sh run_mem_op_test_aff.sh 3 &
sh run_mem_op_test_aff.sh 4 &
sh run_mem_op_test_aff.sh 5 &
sh run_mem_op_test_aff.sh 6 &
sh run_mem_op_test_aff.sh 7 &

sleep 5 
killall memoptest


sh run_mem_op_test_aff.sh 8 &
sh run_mem_op_test_aff.sh 9 &
sh run_mem_op_test_aff.sh 10 &
sh run_mem_op_test_aff.sh 11 &
sh run_mem_op_test_aff.sh 12 &
sh run_mem_op_test_aff.sh 13 &
sh run_mem_op_test_aff.sh 14 &
sh run_mem_op_test_aff.sh 15 &

sleep 5 
killall memoptest

done


