git pull
mvn install
ps aux | grep -ie com.bitpieces.stage.web_service.WebService | awk '{print $2}' | xargs kill -9 
nohup java -cp target/bitpieces_practice-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.bitpieces.stage.web_service.WebService &> /dev/null & 
sleep 10s
wget -r -nd --delete-after --no-check-certificate https://bitpieces.com:8081/pass_encrypt &> /dev/null &