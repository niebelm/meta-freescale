# Copyright (C) 2012-2016 O.S. Systems Software LTDA.
# Copyright (C) 2013-2016 Freescale Semiconductor
# Copyright (C) 2017-2022 NXP

SUMMARY = "Test programs for i.MX BSP"
DESCRIPTION = "Unit tests for the i.MX BSP"
SECTION = "base"
LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/GPL-2.0-or-later;md5=fed54355545ffd980b814dab4a3b312c"

DEPENDS = "alsa-lib freetype libdrm"
DEPENDS:append:mx6-nxp-bsp = " imx-lib"
DEPENDS:append:mx7-nxp-bsp = " imx-lib"
DEPENDS:append:imxvpu = " virtual/imxvpu"

PE = "1"
PV = "7.0+${SRCPV}"

SRC_URI = "git://source.codeaurora.org/external/imx/imx-test.git;protocol=https;branch=${SRCBRANCH} \
           file://memtool_profile"
SRCBRANCH = "lf-5.15.32_2.0.0"
SRCREV = "c640c7e8456b0516851e76adb2acce6b3866b1fb"

S = "${WORKDIR}/git"

inherit module-base use-imx-headers

INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"

PLATFORM:mx6q-nxp-bsp  = "IMX6Q"
PLATFORM:mx6dl-nxp-bsp = "IMX6Q"
PLATFORM:mx6sl-nxp-bsp = "IMX6SL"
PLATFORM:mx6sll-nxp-bsp = "IMX6SL"
PLATFORM:mx6sx-nxp-bsp = "IMX6SX"
PLATFORM:mx6ul-nxp-bsp = "IMX6UL"
PLATFORM:mx7d-nxp-bsp  = "IMX7D"
PLATFORM:mx7ulp-nxp-bsp = "IMX7D"
PLATFORM:mx8-nxp-bsp = "IMX8"

PARALLEL_MAKE = "-j 1"
EXTRA_OEMAKE += "${PACKAGECONFIG_CONFARGS}"

PACKAGECONFIG = "${@bb.utils.contains('DISTRO_FEATURES', 'x11', 'x11', '', d)}"
PACKAGECONFIG:append:imxvpu = " vpu"
PACKAGECONFIG:append:mx8m-nxp-bsp   = " swpdm"

PACKAGECONFIG[x11] = ",,libx11 libxdamage libxrender libxrandr"
PACKAGECONFIG[vpu] = "HAS_VPU=true,HAS_VPU=false,virtual/imxvpu"
PACKAGECONFIG[swpdm] = "HAS_IMX_SW_PDM=true,HAS_IMX_SW_PDM=false,imx-sw-pdm"

do_compile() {
    CFLAGS="${TOOLCHAIN_OPTIONS}"
    oe_runmake V=1 VERBOSE='' \
               CROSS_COMPILE=${TARGET_PREFIX} \
               INC="-I${S}/include \
                    -I${STAGING_INCDIR} \
                    -I${STAGING_INCDIR_IMX}" \
               CC="${CC} -L${STAGING_LIBDIR} ${LDFLAGS}" \
               SDKTARGETSYSROOT=${STAGING_DIR_HOST} \
               LINUXPATH=${STAGING_KERNEL_DIR} \
               KBUILD_OUTPUT=${STAGING_KERNEL_BUILDDIR} \
               PLATFORM=${PLATFORM}
}

do_install() {
    oe_runmake DESTDIR=${D}/unit_tests \
               PLATFORM=${PLATFORM} \
               install

    if [ -e ${WORKDIR}/clocks.sh ]; then
        install -m 755 ${WORKDIR}/clocks.sh ${D}/unit_tests/clocks.sh
    fi
    install -d -m 0755 ${D}/home/root/
    install -m 0644 ${WORKDIR}/memtool_profile ${D}/home/root/.profile
}

FILES:${PN} += "/unit_tests /home/root/.profile"
RDEPENDS:${PN} = "bash"

FILES:${PN}-dbg += "/unit_tests/.debug"
