# Copyright (C) 2021, Rockchip Electronics Co., Ltd
# Released under the MIT license (see COPYING.MIT for the terms)

require recipes-kernel/linux-libc-headers/linux-libc-headers.inc

SRCREV = "190bc3f5e1f67f9f92b9541643759173565f7075"
SRC_URI = " \
	git://github.com/pczaneti/kernel.git;protocol=https;branch=develop-5.10-luckfux-pico; \
"

S = "${WORKDIR}/git"

LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"
