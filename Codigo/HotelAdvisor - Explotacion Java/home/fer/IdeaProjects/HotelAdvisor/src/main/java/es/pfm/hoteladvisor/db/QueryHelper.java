package es.pfm.hoteladvisor.db;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import static com.mongodb.client.model.Filters.*;

public class QueryHelper {

	static MongoDatabase db = DbUtils.getDatabase();

	//USERS

	public static void insertUser(MongoCollection<Document> collection, Document user){
		collection.insertOne(user);
	}

	public static boolean existsUser(MongoCollection<Document> collection, String userName){
		Document user = DbUtils.createDocument("username", userName);
		long count = collection.count(user);
		return count>0;
	}

	public static boolean loginUser(MongoCollection<Document> collection, Document user){
		long count = collection.count(user);
		return count>0;
	}

	public static FindIterable<Document> getUser(MongoCollection<Document> collection, String userName){
		Document user = DbUtils.createDocument("username", userName);
		return collection.find(user);
	}

	public static void updateUser(MongoCollection<Document> collection, Document userToUpdate, Document newUser){
		collection.replaceOne(userToUpdate,newUser);
	}


	//HOTELS & POIS

	public static void updateHotel(MongoCollection<Document> collection, Document hotelToUpdate, Document newHotel){
		collection.replaceOne(hotelToUpdate,newHotel);
	}

	public static MongoCursor<Document> findByDistanceAndFilters(MongoCollection<Document> collection, double longitude, double latitude, double minDistance, double maxDistance, String keyFilter, List<Object> valuesFilter, String filterOperatorType){


		if (keyFilter!=null && valuesFilter!=null) {
			ArrayList<Document> documents = DbUtils.createDocuments(keyFilter, valuesFilter);
			Bson filterDocuments = null;
			if (filterOperatorType.equals("or"))
				filterDocuments = or((Iterable) documents);
			else if (filterOperatorType.equals("and"))
				filterDocuments = and((Iterable) documents);


			return collection.find(and(nearSphere("coord", new Point(new Position(longitude, latitude)), maxDistance, minDistance),
					filterDocuments)).iterator();
		}
		else{
			return collection.find(nearSphere("coord", new Point(new Position(longitude, latitude)), maxDistance, minDistance)).iterator();
		}
	}

	public static Document findByDistance(String collection, double longitude, double latitude, double minDistance, double maxDistance){

		BasicDBObject geoNear = new BasicDBObject();
		geoNear.append("geoNear", collection);
		double[] loc = {longitude,latitude};
		geoNear.append("near", loc);
		geoNear.append("spherical", true);
		geoNear.append("minDistance", (double) minDistance / 6371000 );
		geoNear.append("maxDistance", (double) maxDistance / 6371000 );
		MongoDatabase db = DbUtils.getDatabase();
		Document doc = db.runCommand(geoNear);
		return doc;

	}

}
