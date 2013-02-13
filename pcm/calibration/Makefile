all: calibration

#CC=g++ -Wall
CC=icpc -Wall
OPT= -g -O3 -no-vec -no-ipo -no-ip -mkl=sequential

UNAME:=$(shell uname)

ifeq ($(UNAME), Linux)
LIB= -lpthread -lrt
endif
ifeq ($(UNAME), Darwin)
LIB= -lpthread -lPcmMsr
endif
ifeq ($(UNAME), FreeBSD)
LIB= -lpthread
endif

calibration: ../cpucounters.o ../msr.o ../pci.o ../measuring_core.o
	 $(CC) $(CPPFLAGS) $(OPT) ../msr.o ../pci.o ../cpucounters.o ../measuring_core.o calibration.cpp -o calibration $(LIB) 

clean:
	rm -rf calibration
