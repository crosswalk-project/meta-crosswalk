SECURITY_CFLAGS_pn-crosswalk = "${SECURITY_NO_PIE_CFLAGS} ${SECURITY_PIC_CFLAGS}"
SECURITY_LDFLAGS_remove_pn-crosswalk = "-fstack-protector-strong"
SECURITY_LDFLAGS_append_pn-crosswalk = " -fstack-protector-all"
