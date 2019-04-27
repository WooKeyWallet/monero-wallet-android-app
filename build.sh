#!/bin/bash
sudo mkdir -p /opt/android
sudo chown `whoami` /opt/android
cd /opt/android
wget https://dl.google.com/android/repository/android-ndk-r15c-linux-x86_64.zip -O android-ndk-r15c-linux-x86_64.zip && echo "f01788946733bf6294a36727b99366a18369904eb068a599dde8cca2c1d2ba3c  android-ndk-r15c-linux-x86_64.zip"|sha256sum -c
unzip android-ndk-r15c-linux-x86_64.zip
mv android-ndk-r15c ndk-r15c
git clone https://github.com/WooKeyWallet/monero-android-lib.git /tmp/monero-android-lib
cd /tmp/monero-android-lib/external-libs/
find . -name "*.sh" -exec chmod +x {} \;
mkdir -p build/src
make all