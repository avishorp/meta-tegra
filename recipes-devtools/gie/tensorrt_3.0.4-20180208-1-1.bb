DESCRIPTION = "NVIDIA TensorRT (GPU Inference Engine) for deep learning"
HOMEPAGE = "http://developer.nvidia.com/tensorrt"
LICENSE = "Proprietary"
DEPENDS = "dpkg-native"

SRC_URI = "http://developer.download.nvidia.com/devzone/devcenter/mobile/jetpack_l4t/3.2GA/m892ki/JetPackL4T_32_b196/nv-tensorrt-repo-ubuntu1604-ga-cuda9.0-trt3.0.4-20180208_1-1_arm64.deb"
SRC_URI[md5sum] = "5d625140e1a7c92706191b1f4cbc4cce"
SRC_URI[sha256sum] = "a49b1d23acb4f18e587322907860a714781dc8a1cd0607a0122f9ddfab0106b3"

COMPATIBLE_MACHINE = "(tegra186|tegra210)"
PACKAGE_ARCH = "${SOC_FAMILY_PKGARCH}"

PR = "r0"

LIC_FILES_CHKSUM = "file://NvInfer.h;endline=48;md5=85c72aa9eac0882b53808ab7daa49069"

CUDAPATH ?= "/usr/local/cuda-${CUDA_VERSION}"

S = "${WORKDIR}/var/nv-tensorrt-repo-ga-cuda9.0-trt3.0.4-20180208"
B = "${WORKDIR}/build"

do_configure() {
    dpkg-deb --extract ${S}/libnvinfer4_4.0.4-1+cuda9.0_arm64.deb ${B}
    dpkg-deb --extract ${S}/libnvinfer-dev_4.0.4-1+cuda9.0_arm64.deb ${B}
    dpkg-deb --extract ${S}/libnvinfer-samples_4.0.4-1+cuda9.0_arm64.deb ${B}
    # for the LIC_FILES_CHKSUM check
    cp ${B}/usr/include/aarch64-linux-gnu/NvInfer.h ${S}/
}
do_populate_lic[depends] += "${PN}:do_configure"

do_compile() {
    mv ${B}/usr/include/aarch64-linux-gnu/NvUtils.h ${B}/usr/src/tensorrt/samples/sampleCharRNN/
}

do_install() {
    install -d ${D}${includedir}
    install -m 0644 ${B}/usr/include/aarch64-linux-gnu/*.h ${D}${includedir}
    install -d ${D}${libdir}
    for lib in libnvparsers libnvinfer libnvinfer_plugin; do
        install -m 0644 ${B}/usr/lib/aarch64-linux-gnu/$lib.a ${D}${libdir}
        install -m 0644 ${B}/usr/lib/aarch64-linux-gnu/$lib.so.4.0.4 ${D}${libdir}
	ln -s $lib.so.4.0.4 ${D}${libdir}/$lib.so.4
	ln -s $lib.so.4.0.4 ${D}${libdir}/$lib.so
    done
    ln -s libnvparsers.a ${D}${libdir}/libnvcaffe_parser.a
    ln -s libnvparsers.so.4.0.4 ${D}${libdir}/libnvcaffe_parser.so
    ln -s libnvparsers.so.4.0.4 ${D}${libdir}/libnvcaffe_parser.so.4
    ln -s libnvparsers.so.4.0.4 ${D}${libdir}/libnvcaffe_parser.so.4.0.4
    install -d ${D}${prefix}/src
    cp --preserve=mode,timestamps --recursive ${B}/usr/src/tensorrt ${D}${prefix}/src/
}
PACKAGES =+ "${PN}-examples"
FILES_${PN}-examples = "${prefix}/src"

RDEPENDS_${PN} += "libstdc++ cudnn cuda-cublas cuda-cudart"

INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INHIBIT_SYSROOT_STRIP = "1"
INSANE_SKIP_${PN} = "textrel"
