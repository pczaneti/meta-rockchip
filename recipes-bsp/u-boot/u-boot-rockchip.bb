# Copyright (C) 2019, Fuzhou Rockchip Electronics Co., Ltd
# Released under the MIT license (see COPYING.MIT for the terms)

inherit python3-dir

require recipes-bsp/u-boot/u-boot.inc
require recipes-bsp/u-boot/u-boot-common.inc

PROVIDES = "virtual/bootloader"

DEPENDS += "bc-native dtc-native"

PV = "2017.09"

LIC_FILES_CHKSUM = "file://Licenses/README;md5=a2c678cfd4a4d97135585cad908541c6"

SRCREV = "d2da6f0f4fb6adbad65cee6e97f516a9483eab46"
SRCREV_rkbin = "d2da6f0f4fb6adbad65cee6e97f516a9483eab46"
SRC_URI = " \
	git://github.com/LuckfoxTECH/luckfox-pico;protocol=https;branch=main;subpath=sysdrv/source/uboot/u-boot;subdir=git; \
	git://github.com/LuckfoxTECH/luckfox-pico;protocol=https;branch=main;name=rkbin;subpath=sysdrv/source/uboot/rkbin; \
	file://0003-Revert-Makefile-enable-Werror-option.patch \
"

SRCREV_FORMAT = "default_rkbin"

DEPENDS:append = " ${PYTHON_PN}-native"

# Needed for packing BSP u-boot
DEPENDS:append = " coreutils-native ${PYTHON_PN}-pyelftools-native"

do_configure:prepend() {
	# Make sure we use /usr/bin/env ${PYTHON_PN} for scripts
	for s in `grep -rIl python ${S}`; do
		sed -i -e '1s|^#!.*python[23]*|#!/usr/bin/env ${PYTHON_PN}|' $s
	done

	# Support python3
	sed -i -e 's/\(open([^,]*\))/\1, "rb")/' \
		-e 's/print >> \([^,]*\), *\(.*\),*$/print(\2, file=\1)/' \
		-e 's/print \(.*\)$/print(\1)/' \
		${S}/arch/arm/mach-rockchip/make_fit_atf.py

	# Remove unneeded stages from make.sh
	sed -i -e '/^select_tool/d' -e '/^clean/d' -e '/^\t*make/d' -e '/which python2/{n;n;s/exit 1/true/}' ${S}/make.sh

	# Fixup platform(chip) detection
	sed -i "s/PLAT=.*/PLAT=${RK_SOC_FAMILY}/" ${S}/make.sh

	[ ! -e "${S}/.config" ] || make -C ${S} mrproper

	sed -i 's/ found;/ found = NULL;/' ${S}/lib/avb/libavb/avb_slot_verify.c
}

# Generate Rockchip style loader binaries
RK_IDBLOCK_IMG = "idblock.img"
RK_LOADER_BIN = "loader.bin"
RK_TRUST_IMG = "trust.img"

UBOOT_BINARY = "uboot.img"

do_compile:append() {
	cd ${B}

	# Prepare needed files
	for d in make.sh scripts configs arch/arm/mach-rockchip; do
		cp -rT ${S}/${d} ${d}
	done

	if [ -z "${RK_UBOOT_CFG}" ]; then
		RK_UBOOT_CFG=${RK_SOC_FAMILY}
	fi

	# Pack Rockchip loader images
	if [ "${RK_UBOOT_SPL}" ]; then
		# Use U-Boot's SPL
		./make.sh ${RK_UBOOT_CFG} --spl-new
		if ! grep -q "ROCKCHIP_FIT_IMAGE_PACK=y" .config; then
			# Repack SPL for non-FIT U-Boot
			./make.sh --spl
		fi
	else
		# Use Rockchip Miniloader
		./make.sh ${RK_UBOOT_CFG}
	fi
	if [ -f *_download_*.bin ] ; then
		ln -sf *_download_*.bin "${RK_LOADER_BIN}"
	else
		ln -sf *_loader_*.bin "${RK_LOADER_BIN}"
	fi

	# Generate idblock image
	bbnote "${PN}: Generating ${RK_IDBLOCK_IMG}..."
	if ls *.img | grep -q idblock; then
		ln -sf *_idblock_*.img "${RK_IDBLOCK_IMG}"
	else
		./make.sh --idblock
		ln -sf idblock.bin "${RK_IDBLOCK_IMG}"
	fi
}

do_deploy:append() {
	cd ${B}

	for binary in "${RK_IDBLOCK_IMG}" "${RK_LOADER_BIN}" "${RK_TRUST_IMG}";do
		[ -f "${binary}" ] || continue
		install "${binary}" "${DEPLOYDIR}/${binary}-${PV}"
		ln -sf "${binary}-${PV}" "${DEPLOYDIR}/${binary}"
	done
}
