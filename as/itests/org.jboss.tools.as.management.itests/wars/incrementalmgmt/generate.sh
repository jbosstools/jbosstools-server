#!/bin/sh
SERVLET_JAVAX=/home/rob/apps/jboss/unzipped/wildfly-16.0.0.Final.zip.expanded/modules/system/layers/base/javax/servlet/api/main/jboss-servlet-api_4.0_spec-1.0.0.Final.jar
SERVLET_JAKARTA=/home/rob/apps/jboss/unzipped/wildfly-27.0.0.Final.zip.expanded/modules/system/layers/base/jakarta/servlet/api/main/jakarta.servlet-api-6.0.0.jar

rm -rf util javaxWar jakartaWar
mkdir util

echo "Building Utility jar"
mkdir util/classes
cp UtilModelOriginal.jav UtilModel.java
javac --release 8 UtilModel.java
mv UtilModel.class util/classes/UtilModel_original.class
rm UtilModel.java

cp UtilModelModified.jav UtilModel.java
javac --release 8 UtilModel.java
mv UtilModel.class util/classes/UtilModel_modified.class
rm UtilModel.java

mkdir util/explodedjar
mkdir util/explodedjar/META-INF
mkdir util/explodedjar/util
mkdir util/explodedjar/util/pak
cp MANIFEST.MF util/explodedjar/META-INF
cp util/classes/UtilModel_original.class util/explodedjar/util/pak/UtilModel.class

cd util/explodedjar
zip -r ../UtilOne.jar *
cd ../../

echo "Building javax servlet war"
mkdir javaxWar
mkdir javaxWar/classes
cp TigerServJavaxOriginal.jav TigerServ.java
javac --release 8 -classpath $SERVLET_JAVAX:util/UtilOne.jar TigerServ.java
mv TigerServ.class javaxWar/classes/TigerServ_original.class
rm TigerServ.java

cp TigerServJavaxModified.jav TigerServ.java
javac --release 8 -classpath $SERVLET_JAVAX:util/UtilOne.jar TigerServ.java
mv TigerServ.class javaxWar/classes/TigerServ_modified.class
rm TigerServ.java

mkdir javaxWar/exploded/
mkdir javaxWar/exploded/META-INF
mkdir javaxWar/exploded/WEB-INF
mkdir javaxWar/exploded/WEB-INF/lib
mkdir javaxWar/exploded/WEB-INF/classes
mkdir javaxWar/exploded/WEB-INF/classes/my
mkdir javaxWar/exploded/WEB-INF/classes/my/pak

cp MANIFEST.MF javaxWar/exploded/META-INF/
cp util/UtilOne.jar javaxWar/exploded/WEB-INF/lib
cp javaxWar/classes/TigerServ_original.class javaxWar/exploded/WEB-INF/classes/my/pak/TigerServ.class
cd javaxWar/exploded
zip -r ../out_with_jar.war *
cd ../../



echo "Building jakarta servlet war"
mkdir jakartaWar
mkdir jakartaWar/classes
cp TigerServJakartaOriginal.jav TigerServ.java
javac -classpath $SERVLET_JAKARTA:util/UtilOne.jar TigerServ.java
mv TigerServ.class jakartaWar/classes/TigerServ_original.class
rm TigerServ.java

cp TigerServJakartaModified.jav TigerServ.java
javac -classpath $SERVLET_JAKARTA:util/UtilOne.jar TigerServ.java
mv TigerServ.class jakartaWar/classes/TigerServ_modified.class
rm TigerServ.java

mkdir jakartaWar/exploded/
mkdir jakartaWar/exploded/META-INF
mkdir jakartaWar/exploded/WEB-INF
mkdir jakartaWar/exploded/WEB-INF/lib
mkdir jakartaWar/exploded/WEB-INF/classes
mkdir jakartaWar/exploded/WEB-INF/classes/my
mkdir jakartaWar/exploded/WEB-INF/classes/my/pak

cp MANIFEST.MF jakartaWar/exploded/META-INF/
cp util/UtilOne.jar jakartaWar/exploded/WEB-INF/lib
cp jakartaWar/classes/TigerServ_original.class jakartaWar/exploded/WEB-INF/classes/my/pak/TigerServ.class
cd jakartaWar/exploded
zip -r ../out_with_jar.war *
cd ../../




