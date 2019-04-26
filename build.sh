#!/bin/bash
sudo mkdir -p /opt/android
sudo chown `whoami` /opt/android
cd /opt/android
wget https://dl.google.com/android/repository/android-ndk-r15c-linux-x86_64.zip -O android-ndk-r15c-linux-x86_64.zip
unzip android-ndk-r15c-linux-x86_64.zip
mv android-ndk-r15c ndk-r15c
git clone https://github.com/WooKeyWallet/monero-android-lib.git /tmp/monero-android-lib
cd /tmp/monero-android-lib/external-libs/
find . -name "*.sh" -exec chmod +x {} \;
mkdir -p build/src
make all