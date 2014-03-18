cd /home/barni/xcode/ba_zajzon/source/mobicents/presence_src/sip-event/server/publication/sbb/;
mvn clean install;
cd target;
mv sip-event-publication-control-sbb-1.1.0-SNAPSHOT.jar sip-event-publication-control-sbb-1.0.0.FINAL.jar;
ssh sipadmin@cloud37.dbis.rwth-aachen.de << 'ENDSSH'
cd /home/sipadmin/source/mobicents/presence_binary/server/default/deploy/mobicents-sip-presence/4-slee/;
if [ ! -d tmp ]; then 
	mkdir tmp;
fi;
cd tmp;
jar xf ../integrated-server-slee-services-du-1.0.0.FINAL.jar;
ENDSSH
scp -r sip-event-publication-control-sbb-1.0.0.FINAL.jar sipadmin@cloud37.dbis.rwth-aachen.de:/home/sipadmin/source/mobicents/presence_binary/server/default/deploy/mobicents-sip-presence/4-slee/tmp/jars/;
ssh sipadmin@cloud37.dbis.rwth-aachen.de << 'ENDSSH'
cd /home/sipadmin/source/mobicents/presence_binary/server/default/deploy/mobicents-sip-presence/4-slee/;
rm -rf integrated-server-slee-services-du-1.0.0.FINAL.jar;
cd tmp;
jar -cvf integrated-server-slee-services-du-1.0.0.FINAL.jar jars META-INF services;
mv integrated-server-slee-services-du-1.0.0.FINAL.jar ../ ;
cd .. ;
rm -rf tmp;
ENDSSH
