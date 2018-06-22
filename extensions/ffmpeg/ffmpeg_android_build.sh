#!/usr/bin/env bash

#setup FFMPEG Expension related Paths
export EXOPLAYER_ROOT="${HOME}/WORKSPACE/THIRD_PARTY/EXOPLAYER/v2"
export FFMPEG_EXT_PATH="${EXOPLAYER_ROOT}/extensions/ffmpeg/src/main"
echo "FFMPEG_EXT_PATH = $FFMPEG_EXT_PATH"

#setup NDK related paths
#export NDK_PATH="${HOME}/Library/Android/sdk/ndk-bundle"
export NDK_PATH="${HOME}/Softwares/android-ndk-r15c"
export NDK_PLATFORM_VERSION=14

#setup HOST PLATFORM related paths
#export HOST_PLATFORM="linux-x86_64"
export HOST_PLATFORM="darwin-x86_64" #~/Library/Android/sdk/ndk-bundle/toolchains/arm-linux-androideabi-4.9/prebuilt/darwin-x86_64/

export ADDITIONAL_CONFIGURE_FLAG=
export COMMON_OPTIONS="\
    --target-os=android \
    --disable-static \
    --enable-shared \
    --disable-doc \
    --disable-programs \
    --disable-everything \
    --disable-avdevice \
    --disable-avformat \
    --disable-swscale \
    --disable-postproc \
    --disable-avfilter \
    --disable-symver \
    --disable-swresample \
    --enable-avresample \
    --enable-decoder=vorbis \
    --enable-decoder=opus \
    --enable-decoder=flac \
    --enable-decoder=aac \
    --enable-decoder=ac3 \
    --enable-decoder=eac3 \
    "

cd "${FFMPEG_EXT_PATH}/jni"
echo "Gonna fetch ffmpeg : $(pwd)"

(git -C ffmpeg pull || git clone git://source.ffmpeg.org/ffmpeg ffmpeg)

cd ffmpeg
echo "Inside ffmpeg : $(pwd)"

#CONFIGURE_OUT="$( \
./configure \
    --libdir=android-libs/armeabi-v7a \
    --arch=arm \
    --cpu=armv7-a \
    --cross-prefix="${NDK_PATH}/toolchains/arm-linux-androideabi-4.9/prebuilt/${HOST_PLATFORM}/bin/arm-linux-androideabi-" \
    --sysroot="${NDK_PATH}/platforms/android-9/arch-arm/" \
    --sysinclude="${NDK_PATH}/sysroot" \
    --extra-cflags="-march=armv7-a -mfloat-abi=softfp" \
    --extra-ldflags="-Wl,--fix-cortex-a8" \
    --extra-ldexeflags=-pie \
    ${COMMON_OPTIONS} \
    || exit 1
    #)"
    #--sysroot="${NDK_PATH}/platforms/android-15/arch-arm/" \
    # -isystem=${NDK_PATH}/platforms/android-15/arch-arm/

make -j4 && make install-libs

make clean  && ./configure \
    --libdir=android-libs/arm64-v8a \
    --arch=aarch64 \
    --cpu=armv8-a \
    --cross-prefix="${NDK_PATH}/toolchains/aarch64-linux-android-4.9/prebuilt/${HOST_PLATFORM}/bin/aarch64-linux-android-" \
    --sysroot="${NDK_PATH}/platforms/android-21/arch-arm64/" \
    --extra-ldexeflags=-pie \
    ${COMMON_OPTIONS} \
    || exit 1

make -j4 && make install-libs

make clean && ./configure \
    --libdir=android-libs/x86 \
    --arch=x86 \
    --cpu=i686 \
    --cross-prefix="${NDK_PATH}/toolchains/x86-4.9/prebuilt/${HOST_PLATFORM}/bin/i686-linux-android-" \
    --sysroot="${NDK_PATH}/platforms/android-9/arch-x86/" \
    --extra-ldexeflags=-pie \
    --disable-asm \
    ${COMMON_OPTIONS} \
    || exit 1


make clean && ./configure \
    --libdir=android-libs/x86_64 \
    --arch=x86_64 \
    --cpu=i686 \
    --cross-prefix="${NDK_PATH}/toolchains/x86-4.9/prebuilt/${HOST_PLATFORM}/bin/i686-linux-android-" \
    --sysroot="${NDK_PATH}/platforms/android-9/arch-x86/" \
    --extra-ldexeflags=-pie \
    --disable-asm \
    ${COMMON_OPTIONS} \
    || exit 1

make -j4 && make install-libs
make clean

