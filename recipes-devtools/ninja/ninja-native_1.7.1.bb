DESCRIPTION = "Ninja is a small build system with a focus on speed."
LICENSE = "Apache-2"
HOMEPAGE = "https://ninja-build.org"

inherit native

LIC_FILES_CHKSUM = "file://COPYING;md5=a81586a64ad4e476c791cda7e2f2c52e"

SRC_URI = "https://github.com/ninja-build/${BPN}/archive/v${PV}.tar.gz"
SRC_URI[md5sum] = "e45bda009319f9af5385bb79e783da9f"
SRC_URI[sha256sum] = "51581de53cf4705b89eb6b14a85baa73288ad08bff256e7d30d529155813be19"

UPSTREAM_CHECK_URI = "https://github.com/ninja-build/${BPN}/releases"

B = "${WORKDIR}/build"

do_compile() {
    rm -fr ${B}
    mkdir -p ${B}
    cd ${B}
    python ${S}/configure.py --bootstrap --verbose
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${B}/ninja ${D}${bindir}/ninja
}
