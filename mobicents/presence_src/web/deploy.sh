jar -cvf devices.war *.jsp WEB-INF; scp devices.war sipadmin@cloud37.dbis.rwth-aachen.de:/home/sipadmin/source/mobicents/presence_binary/server/default/deploy;
rm devices.war
