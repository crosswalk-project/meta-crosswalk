require chromium.inc
require chromium-unbundle.inc

# inherit insane

SRC_URI += " \
        file://0001-make-use-of-existing-gn-args-in-ui-build-config.patch \
        file://0002-gn-Stop-asserting-on-use_gconf-when-looking-for-atk.patch \
        file://0003-gn-Stop-making-use_atk-depend-on-use_gconf.patch \
        file://0004-make-use-of-use_gconf-use_glib-gn-args-in-content-br.patch \
        file://0001-Remove-base-internal-InotifyReader-s-destructor.patch \
        file://0001-gn-Use-a-separate-flag-for-enabling-libgnome-keyring.patch \
        file://0001-Rework-v8_target_arch-target_cpu-approach-to-use-v8_.patch \
        file://0002-Land-chromium-side-work-to-clean-up-handling-of-v8_t.patch \
        file://0003-gn-Make-v8_target_arch-a-GN-declare_arg.patch \
        file://0004-gn-Fix-setting-v8_target_arch-default.patch \
        file://0005-Update-GN-build-to-use-v8_target_cpu-instead-of-v8_t.patch \
        file://0006-Try-to-reland-v8_snapshot-GN-build-changes-take-3.patch \
        file://0007-gn-Fix-another-ia32-x86-typo.patch \
        file://0008-gn-Set-correct-defaults-for-some-gn-args.patch \
        file://0009-Land-v8-side-changes-to-switch-to-v8_current_cpu-in-.patch \
        file://0010-Attempt-4-to-land-Fix-double-building-of-v8-in-GN-bu.patch \
        "

DEPENDS = "\
    alsa-lib \
    atk \
    bison-native \
    chromium-native \
    dbus \
    expat \
    flac \
    fontconfig \
    freetype \
    glib-2.0 \
    gtk+ \
    harfbuzz \
    libevent \
    libwebp \
    libx11 \
    libxcomposite \
    libxcursor \
    libxdamage \
    libxext \
    libxfixes \
    libxi \
    libxml2 \
    libxrandr \
    libxrender \
    libxscrnsaver \
    libxslt \
    libxtst \
    ninja-native \
    nspr \
    nss \
    pango \
    pciutils \
    pkgconfig-native \
    ${@bb.utils.contains('DISTRO_FEATURES', 'pulseaudio', 'pulseaudio', '', d)} \
    virtual/libgl \
    "
DEPENDS_append_x86 = "yasm-native"
DEPENDS_append_x86-64 = "yasm-native"

# The wrapper script we use from upstream requires bash.
RDEPENDS_${PN} = "bash"

# Base GN arguments, mostly related to features we want to enable or disable.
GN_ARGS = "\
        is_debug=false \
        use_cups=false \
        use_gconf=false \
        use_gnome_keyring=false \
        use_kerberos=false \
        use_pulseaudio=${@bb.utils.contains('DISTRO_FEATURES', 'pulseaudio', 'true', 'false', d)} \
        "

# NaCl support depends on the NaCl toolchain that needs to be built before NaCl
# itself. The whole process is quite cumbersome so we just disable the whole
# thing for now.
GN_ARGS += "enable_nacl=false"

# We do not want to use Chromium's own Debian-based sysroots, it is easier to
# just let Chromium's build system assume we are not using a sysroot at all and
# let Yocto handle everything.
GN_ARGS += "use_sysroot=false"

# Toolchains we will use for the build. We need to point to the toolchain file
# we've created, set the right target architecture and make sure we are not
# using Chromium's toolchain (bundled clang, bundled binutils etc).
GN_ARGS += '\
        custom_toolchain="//build/toolchain/yocto:yocto_target" \
        host_toolchain="//build/toolchain/yocto:yocto_native" \
        is_clang=false \
        linux_use_bundled_binutils=false \
        target_cpu="${@gn_arch_name(d)}" \
        '

# This function translates between Yocto's TARGET_ARCH values and the ones
# expected by GN.
def gn_arch_name(d):
    import re
    target_arch = d.getVar("TARGET_ARCH", True)
    if re.match(r"i[356]86", target_arch):
        return "x86"
    elif target_arch == "x86_64":
        return "x64"
    elif target_arch == "arm":
        # FIXME(rakuco): no idea if this works.
        return "arm"
    elif target_arch == "aarch64":
        return "arm64"
    elif target_arch == "mipsel":
        return "mipsel"
    else:
        bb.msg.fatal("Unknown TARGET_ARCH value.")

do_configure() {
	mkdir -p ${S}/build/toolchain/yocto
	cat > ${S}/build/toolchain/yocto/BUILD.gn <<EOF
 import("//build/config/sysroot.gni")
 import("//build/toolchain/gcc_toolchain.gni")
 gcc_toolchain("yocto_native") {
   cxx = "${BUILD_CXX}"
   cc = "${BUILD_CC}"
   ar = "${BUILD_AR}"
   ld = cxx
   nm = "${BUILD_NM}"
   readelf = "${BUILD_PREFIX}readelf"
   is_clang = false
   toolchain_cpu = "x64"
   toolchain_os = "linux"
   extra_cflags = "${BUILD_CFLAGS}"
   extra_cppflags = "${BUILD_CPPFLAGS}"
   extra_cxxflags = "${BUILD_CXXFLAGS}"
   extra_ldflags = "${BUILD_LDFLAGS}"
 }
 gcc_toolchain("yocto_target") {
   cxx = "${CXX}"
   cc = "${CC}"
   ar = "${AR}"
   ld = cxx
   nm = "${NM}"
   readelf = "${TARGET_PREFIX}readelf"
   is_clang = false
   toolchain_cpu = "${@gn_arch_name(d)}"
   toolchain_os = "linux"
   extra_cflags = "${TARGET_CFLAGS}"
   extra_cppflags = "${TARGET_CPPFLAGS}"
   extra_cxxflags = "${TARGET_CXXFLAGS} -Wno-strict-overflow"
   extra_ldflags = "${TARGET_LDFLAGS}"
 }
EOF

	cd ${S}

	# ./build/linux/unbundle/remove_bundled_libraries.py ${THIRD_PARTY_TO_PRESERVE}
	./build/linux/unbundle/replace_gn_files.py --system-libraries ${GN_UNBUNDLE_LIBS}

	gn gen --args='${GN_ARGS}' //out/Release
}

do_compile() {
	ninja -C ${S}/out/Release -v chrome chrome_sandbox
}

do_install() {
	install -d ${D}${bindir}
	install -d ${D}${libdir}/chromium
	install -d ${D}${libdir}/chromium/locales

	# A wrapper for the proprietary Google Chrome version already exists.
	# We can just use that one instead of reinventing the wheel.
	WRAPPER_FILE=${S}/chrome/installer/linux/common/wrapper
	sed -i "s,@@CHANNEL@@,stable,g" ${WRAPPER_FILE}
	sed -i "s,@@PROGNAME@@,chromium-bin,g" ${WRAPPER_FILE}
	install -m 0755 ${WRAPPER_FILE} ${D}${libdir}/chromium/chromium-wrapper
	ln -s ${libdir}/chromium/chromium-wrapper ${D}${bindir}/chromium

	install -m 4755 ${S}/out/Release/chrome_sandbox ${D}${libdir}/chromium/chrome-sandbox
	install -m 0755 ${S}/out/Release/chrome ${D}${libdir}/chromium/chromium-bin
	install -m 0644 ${S}/out/Release/*.bin ${D}${libdir}/chromium/
	install -m 0644 ${S}/out/Release/chrome_*.pak ${D}${libdir}/chromium/
	install -m 0644 ${S}/out/Release/icudtl.dat ${D}${libdir}/chromium/icudtl.dat
	install -m 0644 ${S}/out/Release/resources.pak ${D}${libdir}/chromium/resources.pak

	install -m 0644 ${S}/out/Release/locales/*.pak ${D}${libdir}/chromium/locales/
}

# FILES_${PN} = "${bindir}/xwalk ${libdir}/xwalk/*"
# FILES_${PN}-dbg = "${bindir}/.debug/ ${libdir}/xwalk/.debug/"
# PACKAGE_DEBUG_SPLIT_STYLE = "debug-without-src"

INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
