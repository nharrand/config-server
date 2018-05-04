#!/bin/bash


#SOSIFIER=$1
SOSIFIER="/sosie/main.jar"
LOGDIR="log"
URL="127.0.0.1:8080"
mkdir log
chmod +rw log
#loop


while true; do 
	#get config
	curl -X GET -i http://$URL/getConfig -o tmp.json
	#if Answer is not 200 stop
	STATUS=`cat tmp.json | head -n 1 | cut -d ' ' -f2`

	if [ $STATUS -ne "200" ]
	then
		exit $STATUS
	fi

	#get header > property file
	cat tmp.json | grep -v "{" | grep -v "^[A-Z]" | sed 's/: /=/' > properties.properties


	FILE=`cat properties.properties | grep "transformation.directory" | cut -d '=' -f2 | sed 's/\r//'`
	RESULT=`cat properties.properties | grep "result" | cut -d '=' -f2 | sed 's/\r//'`
	LOGF=`cat properties.properties | grep "transformation.directory" | cut -d '=' -f2 | sed 's/\r//'`

	LOG="$LOGDIR/$LOGF"

	mkdir -p "$(dirname "$FILE")"
	mkdir -p "$(dirname "$RESULT")"
	mkdir -p "$(dirname "$LOG")"

	#get config > config
	cat tmp.json | grep "{" > $FILE

	#Run sosifier
	java -jar $SOSIFIER properties.properties > $LOG 2>&1
	STATUS=$?
	CONFIG=`echo $RESULT | sed 's/output\///'`
	#if something goes wrong post error
	if [ $STATUS -ne 0 ]
	then
		curl -i http://$URL/postError  -d @$LOG --header "Content-Type: text/plain" --header "transformation.directory:$CONFIG" -o out
	else
		curl -i http://$URL/postResult  -d @$RESULT --header "Content-Type: application/json" --header "transformation.directory:$CONFIG" -o out
	fi
	STATUS=`cat out | grep "200 OK" | wc -l`
	echo "POST STATUS $STATUS" >> $LOG
	#if answer is 200 clean up and repeat
	# else keep file in local errors.
	if [ $STATUS -ge 1 ]
	then
		rm $FILE
		rm $RESULT
	fi
	rm tmp.json
	rm out
	rm properties.properties

done

#todo
#eventually expend server to handle multi file config with a system getConfigKey and file list, getFile(file,key)
