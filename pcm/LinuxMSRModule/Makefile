obj-$(CONFIG_X86_MSR) += msr.o

all:
	make -C /lib/modules/$(shell uname -r)/build M=$(PWD) modules

clean:
	make -C /lib/modules/$(shell uname -r)/build M=$(PWD) clean
	
install: all
	rm -rf /lib/modules/$(shell uname -r)/kernel/arch/x86/kernel/msr.ko
	cp msr.ko /lib/modules/$(shell uname -r)/kernel/arch/x86/kernel/msr.ko