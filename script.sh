#!/bin/bash


#https://github.com/HemanthJabalpuri/AndroidExplorer/blob/master/build_new.sh

#Replace by your own
AAPT2=/home/baptiste/comp/android/sdk/build-tools/30.0.2/aapt2
D8=/home/baptiste/comp/android/sdk/build-tools/30.0.2/d8
ZIPALIGN=/home/baptiste/comp/android/sdk/build-tools/30.0.2/zipalign
APKSIGNER=/home/baptiste/comp/android/sdk/build-tools/30.0.2/apksigner

if [[ ! ${AAPT2:+1} || ! ${D8:+1} || ! ${ZIPALIGN:+1} || ! ${APKSIGNER:+1} ]]
then
    echo "you must define the variables of all the compilation tools "
    echo "exit"
    exit 1
fi

SeeTemporaryFolder=true

#Change if you whant but it's pointless
Storepass=Passwrd

echo "CLEANING"
adb shell "pm uninstall --user 0 com.pignier.instagramdm"
rm -fr obj
rm -fr bin
rm -fr gen

#Create keystore if you dont have one
if [ ! -f MainKeystore.keystore ]; then
    echo "Lets generate a keystore to sign your apk : "
    keytool -genkeypair -v -storetype pkcs12 -dname "cn=A, ou=B, o=C, c=D" -keystore MainKeystore.keystore -storepass $Storepass -alias MainKey -keyalg RSA -keysize 2048 -validity 10000
fi

#Find all usefull files
thereislibs=$(find libs -type f -name '*.jar')
if [ ! -z "thereislibs" ]; then
	for i in libs/*.jar; do
		javac_libs="$javac_libs:$i"
	done
	javac_libs="${javac_libs#:}"
fi

echo "AAPT2"
mkdir -p gen bin
$AAPT2 compile -v --dir res/ -o res/res.zip
$AAPT2 link -v -I libs/android.jar  --auto-add-overlay --manifest src/AndroidManifest.xml --java gen/  -o bin/AndroidTest.unsigned.unalign.apk res/res.zip 



all_R_java=$(find gen -type f -name '*.java') #Find all R.java (from lib and src) and .java files
all_code_java=$(find src -type f -name '*.java')


echo "JAVAC"
mkdir -p obj
javac -Xlint:deprecation -Xdiags:verbose -Xlint:unchecked -d obj/ -sourcepath gen:src -classpath $javac_libs $all_R_java $all_code_java 

if ! $SeeTemporaryFolder ; then
	rm -fr gen/
fi

echo "D8"
jar cf obj/all.jar obj/

$D8 --release --lib libs/android.jar --output bin/ obj/all.jar
             
if ! $SeeTemporaryFolder ; then
	rm -fr obj/
fi

echo "ZIP"
zip -uj bin/AndroidTest.unsigned.unalign.apk bin/classes.dex 

echo "ZIPALIGN"
$ZIPALIGN -vf 4 bin/AndroidTest.unsigned.unalign.apk bin/AndroidTest.unsigned.apk

echo "APKSIGNER"
$APKSIGNER sign  -v --ks MainKeystore.keystore --ks-key-alias MainKey --ks-pass pass:$Storepass --out bin/AndroidTest.apk bin/AndroidTest.unsigned.apk 

echo "ADB"
adb install -r bin/AndroidTest.apk

if ! $SeeTemporaryFolder ; then
	rm -fr bin/
fi