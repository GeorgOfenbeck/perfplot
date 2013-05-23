obj-m += perfplot.o

KERNEL=$(shell uname -r)

all:
	make -C /lib/modules/$(KERNEL)/build M=$(PWD) modules

clean:
	make -C /lib/modules/$(KERNEL)/build M=$(PWD) clean
	
install:
	make -C /lib/modules/$(KERNEL)/build M=$(PWD) modules_install
	echo "KERNEL==\"perfplot[0-9]*\", NAME=\"cpu/%n/perfplot\", MODE=\"0666\"" > /etc/udev/rules.d/20-perftool.rules