# chromium-native is actually just the GN binary used to configure Chromium.
# It is not released separately, and each Chromium release is only expected to
# work with the GN version provided with it.

require chromium.inc

inherit native

# The build system expects the linker to be invoked via the compiler. If we use
# the default value for BUILD_LD, it will fail because it does not recognize
# some of the arguments passed to it.
BUILD_LD = "${CXX}"

do_configure() {
    rm -fr ${S}/out_bootstrap
    python ${S}/tools/gn/bootstrap/bootstrap.py --verbose --no-clean --no-rebuild
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${S}/out_bootstrap/gn ${D}${bindir}/gn
}
