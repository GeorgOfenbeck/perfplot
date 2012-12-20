


for i in {0..31}; do sh run_mem_op_test_aff.sh $i;sleep 2; killall memoptest ; done;


