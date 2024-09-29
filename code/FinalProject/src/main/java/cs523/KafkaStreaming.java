package cs523;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.io.compress.Compression.Algorithm;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;


public class KafkaStreaming
{
	private static final String HBASE_TABLE_NAME = "gold";
	private static final String CF_PRICE = "price";
	private static final String CF_TRANSACTION = "volume";

	public static void main(String[] args) throws Exception
	{
		// check and create HBASE table
		createHBaseTable();
		
		// Spark Configuration
		SparkConf conf = new SparkConf().setAppName("KafkaStreaming").setMaster("local[*]");
		JavaSparkContext sparkContext = new JavaSparkContext(conf);
		JavaStreamingContext streamingContext = new JavaStreamingContext(sparkContext,new Duration(5000));
		
		// Kafka Configuration
		String kafkaBootstrapServers = "localhost:9092";
		String topic = "gold-topic";
		
		Map<String, Object> kafkaParams = new HashMap<>();
		kafkaParams.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
		kafkaParams.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		kafkaParams.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		kafkaParams.put(ConsumerConfig.GROUP_ID_CONFIG, "console-consumer-54206");
		kafkaParams.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		kafkaParams.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
		kafkaParams.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);

		// Create a DStream for the Kafka topic
		JavaDStream<String> stream = KafkaUtils.createDirectStream(
				streamingContext, 
				LocationStrategies.PreferConsistent(), 
				ConsumerStrategies.Subscribe(Collections.singleton(topic), kafkaParams))
				.map(record -> record.value().toString());
		
		// Process each RDD in the DStream
		stream.foreachRDD(rdd ->{
			System.out.println("processing a new RDD with" + rdd.count()+ "records.");
			if(!rdd.isEmpty()){
				// Save data to HBase
				rdd.foreachPartition(partitionOfRecords -> saveToHBase(partitionOfRecords));
			}else{
				System.out.println("No new data");
			}
		});
		streamingContext.start();
		streamingContext.awaitTermination();
	}
	
	private static void createHBaseTable(){
		Configuration hbaseConfig = HBaseConfiguration.create();
		try (Connection connection = ConnectionFactory.createConnection(hbaseConfig);
				Admin admin = connection.getAdmin())
		{
			// create table descriptor
			HTableDescriptor table = new HTableDescriptor(TableName.valueOf(HBASE_TABLE_NAME));
			table.addFamily(new HColumnDescriptor(CF_PRICE).setCompressionType(Algorithm.NONE));
			table.addFamily(new HColumnDescriptor(CF_TRANSACTION));
			if (!admin.tableExists(table.getTableName()))
			{
				admin.createTable(table);
				System.out.print("Table created.... "+ table.getTableName());
			}else{
				System.out.print("Table already exists.... "+ table.getTableName());
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private static void saveToHBase(Iterator<String> rdd){
		Configuration hbaseConfig = HBaseConfiguration.create();
		try (Connection connection = ConnectionFactory.createConnection(hbaseConfig);
				Table table = connection.getTable(TableName.valueOf(HBASE_TABLE_NAME)))
		{
			rdd.forEachRemaining(data ->{
				// Assuming data is in key,value
				String[] parts = data.split(",");
				if(parts.length == 6){
					String key = parts[0];
					String closePrice = parts[1];
					String volume = parts[2];
					String openPrice = parts[3];
					String highPrice = parts[4];
					String lowPrice = parts[5];
					
					Put put = new Put(Bytes.toBytes(key));
					put.addColumn(Bytes.toBytes(CF_PRICE),Bytes.toBytes("Close/Last"),Bytes.toBytes(closePrice));
					put.addColumn(Bytes.toBytes(CF_PRICE),Bytes.toBytes("Open"),Bytes.toBytes(openPrice));
					put.addColumn(Bytes.toBytes(CF_PRICE),Bytes.toBytes("High"),Bytes.toBytes(highPrice));
					put.addColumn(Bytes.toBytes(CF_PRICE),Bytes.toBytes("Low"),Bytes.toBytes(lowPrice));
					put.addColumn(Bytes.toBytes(CF_TRANSACTION),Bytes.toBytes("Volume"),Bytes.toBytes(volume));
					try{
						table.put(put);
						System.out.println("Inserted data into HBase: "+ key + "->"+volume);
					}catch(IOException e){
						e.printStackTrace();
					}
				}
			});
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
}