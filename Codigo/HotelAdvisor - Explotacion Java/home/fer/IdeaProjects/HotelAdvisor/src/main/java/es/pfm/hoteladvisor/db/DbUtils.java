package es.pfm.hoteladvisor.db;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;


public class DbUtils {
	
	private static MongoClient client;
	private static MongoDatabase db;

	public static MongoClient getClient(){
		if (client==null) {
			client = new MongoClient();
		}
		return client;
	}
	
	public static MongoDatabase getDatabase(){
		if (db==null)
			db = DbUtils.getClient().getDatabase("HotelAdvisor");
		return db;
	}

	public static ArrayList<Document> createDocuments(String key, List<Object> values){
		ArrayList<Document> documents = new ArrayList<Document>();
		for(Object value: values){
			if (value!=null) {
				documents.add(createDocument(key, value));
			}
		}
		return documents;
	}


	public static Document createDocument(String key, Object value){
		return new Document(key,value);
	}
}