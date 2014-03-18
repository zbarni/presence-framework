jar -cvf devices.war ../web/*.jsp ../web/WEB-INF; scp devices.war sipadmin@cloud37.dbis.rwth-aachen.de:/home/sipadmin/source/mobicents/presence_binary/server/default/deploy;
rm devices.war
