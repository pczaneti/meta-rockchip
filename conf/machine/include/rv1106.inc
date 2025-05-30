# Copyright (c) 2025, Rockchip Electronics Co., Ltd
# Released under the MIT license (see COPYING.MIT for the terms)

require conf/machine/include/arm/armv7a/tune-cortexa7.inc
require conf/machine/include/rockchip-common.inc

SERIAL_CONSOLES ?= "115200;ttyFIQ0"

RK_SOC_FAMILY := "rv1106"

KBUILD_DEFCONFIG := "luckfox_rv1106_linux_defconfig"
