require chromium.inc

inherit native

BUILD_LD = "${CXX}"

do_configure() {
    rm -fr ${S}/out_bootstrap
    python ${S}/tools/gn/bootstrap/bootstrap.py --verbose --no-clean --no-rebuild
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${S}/out_bootstrap/gn ${D}${bindir}/gn
}
