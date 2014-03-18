cd /home/barni/xcode/ba_zajzon/source/mobicents/presence_src/sip-event/server/publication/data/;
mvn clean install;
cd target;
mv sip-event-publication-control-data-1.1.0-SNAPSHOT.jar sip-event-publication-control-data-1.0.0.FINAL.jar;
scp -r sip-event-publication-control-data-1.0.0.FINAL.jar sipadmin@cloud37.dbis.rwth-aachen.de:/home/sipadmin/source/mobicents/presence_binary/server/default/deploy/mobicents-sip-presence/2-data/
