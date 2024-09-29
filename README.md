# CS523DE-Final-Project

## A project based on Kafka, Spark and HBase for Data Streaming

## To develop this project we chose a dataSet which includes the real time gold prices(in USD) from 2012 to 2022.

### This dataset has daily record of the gold price which is feasible for our requirement to store in the HBase database. Later on we can predict the future gold price and its volume of transaction based on the data recorded in the HBase database.

> Metadata of the DataSet

1. Date - Date on which Price is Noted
2. Close - Close Price of the Gold in USD
3. Volume - Sum of buy's and sell's of Gold Commodity
4. Open - Open price of a Gold on that particular day
5. High - High price of Gold on that particular day
6. Low - Low price of Gold on that particular day

> DataSet with sample data  
> ![DataSet](/images/2%20-%20Input%20File.JPG)

### Source of the DataSet: [Click Here](https://www.nasdaq.com/market-activity/commodities/gc:cmx)

### Link to the Demonstration Video: [Click Here](https://mum0-my.sharepoint.com/:v:/g/personal/santosh_manandhar_miu_edu/EXOSKyrL1tBLvl5rXaLf478BZWx-nW5BCmGvDUme673gAQ?e=0NIs3J)

## Workflow of the project

![Project Workflow](/images/Flow%20Diagram.png)

1. Mocked a RealTime Data Producing API where each line of the CSV file (dataset) is extracted as a message.
2. Thus extracted message is passed to Kafka Producer and serialized as a Kafka message with key and value which is produced every two seconds and served via Kafka Topic.
3. Then the messages are consumed by Kafka Consumer which are deserialized and later on ingested in Spark using spark streaming API in the form of DStream.
4. At last each RDD present in the DStream is processed and parsed to extract specific data which is then saved to HBase database.

## Steps taken to develop and run the project

1. At first, Kafka was installed in the Cloudera VM then the kafka broker was started from `/opt/kafka` directory using following command in a new terminal window.  
   `sudo bin/kafka-server-start.sh config/server.properties`
   - Command to start Kafka broker  
     ![Command to start Kafka broker](/images/1%20-%20Start%20Kafka.JPG)
   - Kafka broker up and running  
     ![Kafka broker up and running](/images/1ii%20-%20Start%20Kafka%20-2.JPG)
2. Along with this we also need a Kafka topic where messages could be served and communicated across producer and consumer so we used following command to create a new Kafka topic. (For this project the name of the topic was **gold-topic**)  
   `/opt/kafka/bin/kafka-topics.sh --create --topic gold-topic --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1`

3. Since, our main aim was to develop a data streaming workflow so we created two different Maven projects where one was developed as a Kafka Producer and other as a Kafka Consumer.
   - In the producer project we mocked a real time data producing API by using BufferedReader where each line of the CSV file was extracted as a message and produced via Kafka as Kafka message.
   - Similarly, in the consumer project the messages were consumed by Kafka Consumer from the topic which were ingested as DStream in Spark using spark streaming API. Here, each RDD present in the DStream were again processed and parsed to extract specific data which were then saved to HBase database.
4. Then in order to see all the messages produced by Kafka Producer, we opened up a new terminal window and used following command.  
   `/opt/kafka/bin/kafka-console-consumer.sh --topic gold-topic --from-beginning --bootstrap-server localhost:9092`
   - Command to see Kafka messages  
     ![Command to see Kafka messages](/images/3%20-%20See%20Consumed%20Message.JPG)
5. Now its time to run both projects.

- At first the consumer project was started where the Kafka consumer will be ready to consumer the produced messages and save them to HBase database. Therefore, the project was started where two processes got executed.
  - A new HBase table was created (If it was not created previously). To see whether these changes were applied, a new terminal window was opened up and `hbase shell` command was executed to start the HBase shell. Here, following commands were executed to verify the creation of the table.  
    ![Launched HBase Shell](/images/5%20-%20Launched%20HBase%20Shell.JPG)
  - Kafka Consumer was up and running to check the Kafka messages in the topic.
    ![Started Kafka Streaming](/images/4%20-%20Started%20Kafka%20Streaming.JPG)
- Secondly, the producer project was started where the messages are produced from Kafka Producer every two seconds and served in Kafka Topic. This producer is up and running until all the messages are produced via it.
  ![Started RealTime Producer](/images/6%20-%20Started%20RealTime%20Producer.JPG)  
  After the messages were produced we were then able to see the messages in the previous terminal window where we executed a command to see the messages.
  ![Messages Consumed By Kafka Consumer](/images/7%20-%20Messages%20Consumed%20By%20Kafka%20Consumer.JPG)
- At last, all those messages consumed by Kafka Consumer were deserialized to DStream. Here, each RDD present in the DStream were processed and saved to HBase.
  - While saving the data it was saved with a key and two column families as price and volume.
    ![Messages Saved to HBase](/images/8%20-%20Messages%20Saved%20to%20HBase.JPG)
  - In order to view the table and data present in it, we used Hue easy-to-use interface.
    - Table in Hue  
      ![HBase Table in Hue](/images/9%20-%20HBase%20Table%20in%20Hue.JPG)
    - Data in Gold table  
      ![Data in HBase Table](/images/10%20-%20Data%20in%20HBase%20Table.JPG)

Apart from above mentioned steps, there are few other commands which might be useful while running this project locally. You can go to [this file](/code/Commands.sh) to see other commands.  
Hence, we were able to use the combination of Kafka, Spark and HBase to stream data in realtime and save it to the database. Later on analysis and visualization can be carried out in that data to develop insights.
