# start kafka broker
sudo bin/kafka-server-start.sh config/server.properties

# create kafka topic 
/opt/kafka/bin/kafka-topics.sh --create --topic <topic-name> --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

# list topics
/opt/kafka/bin/kafka-topics.sh --list --bootstrap-server localhost:9092

# produce messages
/opt/kafka/bin/kafka-console-producer.sh --topic <topic-name> --bootstrap-server localhost:9092

# see consumed messages
/opt/kafka/bin/kafka-console-consumer.sh --topic <topic-name> --from-beginning --bootstrap-server localhost:9092

# updating the retention policy of a topic
/opt/kafka/bin/kafka-configs.sh --alter --entity-type topics --entity-name <topic-name> --add-config retention.ms=60000 --bootstrap-server localhost:9092

# remove the retention policy of a topic
/opt/kafka/bin/kafka-configs.sh --alter --entity-type topics --entity-name <topic-name> --remove-config retention.ms --bootstrap-server localhost:9092

# list consumer group 
/opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
