package cs523;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;


public class RealTimeProducer
{

	public static void main(String[] args) throws Exception
	{
		String kafkaBootstrapServers = "localhost:9092";
		String topic = "gold-topic";
		
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,kafkaBootstrapServers);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());
		
		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);
		
		// Read the CSV file
		String csvFile = "/home/cloudera/workspace/RealTimeProducer/input/input.csv";
		String line;
		String csvSplit = ",";
		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			br.readLine();
			while((line = br.readLine()) != null){
				// Use comma as separator
				String[] fields = line.split(csvSplit);
				
				// Extract Date as the Key
				String key = fields[0];
				
				// Create the message 
				String value = String.join(",", fields);
				
				// Produce to Kafka
				ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, key,value);
				producer.send(record);
				Thread.sleep(2000);
				System.out.println("Sent: "+ value);
			}
		} catch(Exception e){
			e.printStackTrace();
		} finally{
			producer.close();
		}
	}
}
