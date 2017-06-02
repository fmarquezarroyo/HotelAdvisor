package es.pfm.hoteladvisor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import dnl.utils.text.table.TextTable;
import es.pfm.hoteladvisor.db.DbUtils;
import es.pfm.hoteladvisor.db.QueryHelper;
import es.pfm.hoteladvisor.model.Hotel;
import es.pfm.hoteladvisor.model.HotelQuery;
import es.pfm.hoteladvisor.model.Poi;
import es.pfm.hoteladvisor.search.ElasticSearch;
import es.pfm.hoteladvisor.search.ElasticSearchQueryHelper;
import es.pfm.hoteladvisor.ui.UserInterface;
import es.pfm.hoteladvisor.utils.IO;
import es.pfm.hoteladvisor.utils.Maths;
import es.pfm.hoteladvisor.utils.PoisTypes;
import es.pfm.hoteladvisor.utils.Util;
import org.bson.Document;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import org.bson.conversions.Bson;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.LoggerFactory;
import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.*;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;


public class HotelAdvisor {

	private static final String menu =
				"-------------------------\n" +
					   "1. Search Hotel \n" +
					   "2. Advanced Search Hotel \n" +
					   "3. Search Hotel POIs \n" +
					   "4. Search POI Hotels \n" +
					   "5. TOP Hotels\n" +
					   "6. Review Hotel \n" +
					   "7. Show User Reviews \n" +
					   "8. Modify User Preferences \n" +
					   "9. Logout \n" +
						"-------------------------\n" +
					   "Choose Option: ";

	private static final String logInMenu =
					"-----------------\n" +
			        "1. Register User \n" +
					"2. Login User \n" +
					"3. Exit \n" +
					"-----------------\n" +
					"Choose Option: ";

	private static final String advancedSearchMenu =
					"-------------------------\n" +
					"1. Search Hotel Features \n" +
					"2. Search Hotel Comments \n" +
					"3. Search Similar Hotels \n" +
					"4. Exit \n" +
					"-------------------------\n" +
					"Choose Option: ";

	private static final String topHotelsMenu =
			  		"--------------------\n" +
					"1. Most Scored \n" +
					"2. Most Reviewed \n" +
					"3. Most Inexpensive \n" +
					"4. Most Sized \n" +
					"5. Exit \n" +
					"---------------------\n" +
					"Choose Option: ";

	private String currentUser = null;
	public IO io;
	private MongoDatabase db;
	private MongoCollection<Document> collectionHotels;
	private MongoCollection<Document> collectionPois;
	private MongoCollection<Document> collectionUsers;
	private UserInterface ui;

	private void run() throws IOException {
		db = DbUtils.getDatabase();
		collectionHotels = db.getCollection("hotels");
		collectionPois = db.getCollection("pois");
		collectionUsers = db.getCollection("users");
		io = IO.getIO();
		ui = UserInterface.getInstance();
		boolean salir = false;
		currentUser = null;
		while (!salir) {
			if (currentUser == null) {
				io.write(logInMenu);
				salir = parseLogInOption();
			} else {
				io.write(menu);
				salir = parseMenuOption();
			}
		}
		io.close();
	}

	private void parseTopHotelsOption() throws IOException {
		String opt = io.read();
		if(!Util.isNumeric(opt)) {
			io.write("Incorrect Option. Please, try again\n");
		}
		else {
			int option = Integer.parseInt(opt);
			switch (option) {
				case 1: //Most Scored
					searchMostScoredHotels();
					break;
				case 2://Most Reviewed
					searchMostReviewedHotels();
					break;
				case 3://Most Inexpensive
					searchMostInexpensiveHotels();
					break;
				case 4: //Most Sized
					searchMostSizedHotels();
					break;
				case 5:
					break;
				default:
					io.write("Incorrect Option. Please, try again");
			}
		}
	}

	private void parseAdvancedSearchHotelOption() throws IOException {
		String opt = io.read();
		if(!Util.isNumeric(opt)) {
			io.write("Incorrect Option. Please, try again\n");
		}
		else {
			int option = Integer.parseInt(opt);
			io.write("\n");
			String userName;
			String userId;
			switch (option) {
				case 1: //Search Hotel Features
					searchHotelFeatures();
					break;
				case 2://Search Hotel Comments
					searchHotelComments();
					break;
				case 3://Search Similar Hotels
					searchSimilarHotels();
					break;
				case 4:
					break;
				default:
					io.write("Incorrect Option. Please, try again");
			}
		}
	}


	private boolean parseMenuOption() throws IOException {
		String opt = io.read();
		if(!Util.isNumeric(opt)) {
			io.write("Incorrect Option. Please, try again\n");
			return false;
		}

		int option = Integer.parseInt(opt);
		io.write("\n");
		String userName;
		String userId;
		switch (option) {
			case 1: //Search Hotel
				searchHotel();
				break;
			case 2: //Advanced Search Hotel
				io.write(advancedSearchMenu);
				parseAdvancedSearchHotelOption();
				break;
			case 3://Search Hotel POIs
				searchHotelPois();
				break;
			case 4://Search POI Hotels
				searchPoiHotels();
				break;
			case 5: //Top Hotels
				io.write(topHotelsMenu);
				parseTopHotelsOption();
				break;
			case 6: //Review Hotel
				reviewHotel();
				break;
			case 7: //Show User Reviews
				showUserReviews();
				break;
			case 8: //Modify User Preferences
				modifyUserPreferences();
				break;
			case 9:
				currentUser = null;
				break;
			default:
				io.write("Incorrect Option. Please, try again");
		}

		return false;

	}

	private boolean parseLogInOption() throws IOException {
		String opt = io.read();
		if(!Util.isNumeric(opt)) {
			io.write("Incorrect Option. Please, try again\n");
			return false;
		}

		int option = Integer.parseInt(opt);
		io.write("\n");
		String userId;
		switch (option) {
			case 1:
				io.write("\nUsername: ");
				String userName = io.read();
				boolean existUser = QueryHelper.existsUser(collectionUsers,userName);
				if (existUser){
					io.write("Username: " + userName + " already exists. Please, try again");
					break;
				}else {

					io.write("\nPassword: ");
					String password = io.read();

					io.write("\nName: ");
					String name = io.read();

					io.write("\nSurname: ");
					String surname = io.read();

					io.write("\nAge: ");
					String age = io.read();
					while (!Util.isNumeric(age)){
						io.write("\nIncorrect value.  Please, try again");
						io.write("\nAge: ");
						age = io.read();
					}

					io.write("\nEmail: ");
					String email = io.read();

					io.write("\nLanguage [es,en]: ");
					String lang = io.read();
					while (!"es".equals(lang) && !"en".equals(lang)){
						io.write("\nIncorrect value. Valid options: es (Spanish) or en (English)");
						io.write("\nLanguage [es,en]: ");
						lang = io.read();
					}

					io.write("\nPois Min Distance (m): ");
					String poisMinDistance = io.read();
					while (!Util.isNumeric(poisMinDistance)){
						io.write("\nIncorrect value.  Please, try again");
						io.write("\nPois Min Distance (m): ");
						poisMinDistance = io.read();
					}

					io.write("\nPois Max Distance (m): ");
					String poisMaxDistance = io.read();
					while (!Util.isNumeric(poisMaxDistance)){
						io.write("\nIncorrect value.  Please, try again");
						io.write("\nPois Max Distance (m): ");
						poisMaxDistance = io.read();
					}

					io.write("\nPois Number Limit: ");
					String poisNumberLimit = io.read();
					while (!Util.isNumeric(poisNumberLimit)){
						io.write("\nIncorrect value.  Please, try again");
						io.write("\nPois Number Limit: ");
						poisNumberLimit = io.read();
					}

					io.write("\nPois Types: ");
					io.write(PoisTypes.pois);
					String poisTypes = io.read();
					while (!Util.areValidTypes(poisTypes)){
						io.write("\nIncorrect value.  Please, try again");
						io.write("\nPois Types: ");
						io.write(PoisTypes.pois);
						poisTypes = io.read();
					}

					io.write("\nTOP Hotels Limit: ");
					String topHotelsLimit = io.read();
					while (!Util.isNumeric(topHotelsLimit)){
						io.write("\nIncorrect value.  Please, try again");
						io.write("\nTOP Hotels Limit: ");
						topHotelsLimit = io.read();
					}

					Document user = new Document();
					user.append("username", userName);
					user.append("password", password);
					user.append("name", name);
					user.append("surname", surname);
					user.append("age", Integer.parseInt(age));
					user.append("email", email);
					user.append("language", lang);
					user.append("poisMinDistance", Double.parseDouble(poisMinDistance));
					user.append("poisMaxDistance", Double.parseDouble(poisMaxDistance));
					user.append("poisNumberLimit", Integer.parseInt(poisNumberLimit));
					user.append("poisTypes", poisTypes);
					user.append("topHotelsLimit", Integer.parseInt(topHotelsLimit));

					try {
						QueryHelper.insertUser(collectionUsers, user);
						io.write("\nUser: " + userName + " has been created succesfully\n");
					}
					catch(Exception e){
						io.write("\nThere has been a problem creating user: " + userName + ". Please, try again");
					}
					break;
				}

			case 2:
				io.write("Username: ");
				userName = io.read();
				existUser = QueryHelper.existsUser(collectionUsers,userName);
				if(!existUser) {
					io.write("Username: " + userName + " doesn't exist. Please, register in 'Register User' menu");
					break;
				}else{
					io.write("\nPassword: ");
					String password = io.read();
					Document user = new Document();
					user.append("username", userName);
					user.append("password", password);
					boolean exist = QueryHelper.loginUser(collectionUsers,user);
					if(exist){
						io.write("\n");
						currentUser = userName;
						break;
					}else{
						io.write("Password: " + password + " is incorrect for user: "+ userName + ". Please, try again");
						break;
					}

				}
			case 3:
				return true;
			default:
				io.write("Incorrect Option. Please, try again");
		}
		return false;
	}

	public static void main(String[] args) throws IOException {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
		rootLogger.setLevel(Level.OFF);

		new HotelAdvisor().run();
	}

	public void searchMostReviewedHotels()throws IOException {

		io.write("Select City:");
		String cityName = io.read();
		Bson filterCity = null;
		Document sort = new Document();
		sort.append("review_nr",-1);
		sort.append("review_score",-1);

		if(cityName!=null && !cityName.equals(""))
			filterCity= Filters.eq("city_hotel", cityName);

		AggregateIterable<Document> hotels = null;

		FindIterable<Document> userIt = QueryHelper.getUser(collectionUsers, currentUser);
		Iterator userIterator = userIt.iterator();
		int topHotelsLimit = 20;
		if (userIterator.hasNext()) {
			Document user = (Document) userIterator.next();
			topHotelsLimit = user.getInteger("topHotelsLimit");
		}

		if(filterCity!=null){
			hotels = collectionHotels.aggregate(Arrays.asList(
					match(filterCity),
					sort(sort),
					limit(topHotelsLimit)
			));
		}else{
			hotels = collectionHotels.aggregate(Arrays.asList(
					sort(sort),
					limit(topHotelsLimit)
			));

		}

		showHotels(hotels);

	}

	public void searchMostScoredHotels() throws IOException{
		io.write("Select City:");
		String cityName = io.read();
		Bson filterCity = null;
		Document sort = new Document();
		sort.append("review_score",-1);
		sort.append("review_nr",-1);

		if(cityName!=null && !cityName.equals(""))
		    filterCity= Filters.eq("city_hotel", cityName);

		AggregateIterable<Document> hotels = null;

		FindIterable<Document> userIt = QueryHelper.getUser(collectionUsers, currentUser);
		Iterator userIterator = userIt.iterator();
		int topHotelsLimit=0;
		if (userIterator.hasNext()) {
			Document user = (Document) userIterator.next();
			topHotelsLimit = user.getInteger("topHotelsLimit");
		}

		if(filterCity!=null){
			hotels = collectionHotels.aggregate(Arrays.asList(
					match(filterCity),
					sort(sort),
					limit(topHotelsLimit)
			));
		}else{
			hotels = collectionHotels.aggregate(Arrays.asList(
					sort(sort),
					limit(topHotelsLimit)
			));

		}

		showHotels(hotels);

	}

	public void searchMostInexpensiveHotels() throws IOException{

		io.write("Select City:");
		String cityName = io.read();
		Bson filterCity = null;
		Document sort = new Document();
		sort.append("min_rate_EUR",1);
		sort.append("review_score",-1);

		FindIterable<Document> userIt = QueryHelper.getUser(collectionUsers, currentUser);
		Iterator userIterator = userIt.iterator();
		int topHotelsLimit = 20;
		if (userIterator.hasNext()) {
			Document user = (Document) userIterator.next();
			topHotelsLimit = user.getInteger("topHotelsLimit");
		}

		Bson filters = null;
		Bson filterMinRate = Filters.gt("min_rate_EUR", new Double(0));
		if(cityName!=null && !cityName.equals("")) {
			filterCity = Filters.eq("city_hotel", cityName);
			filters = Filters.and(filterCity,filterMinRate);
		}else{
			filters = filterMinRate;
		}

		AggregateIterable<Document> hotels = collectionHotels.aggregate(Arrays.asList(
					match(filters),
					sort(sort),
					limit(topHotelsLimit)
			));

		showHotels(hotels);

	}

	public void searchMostSizedHotels() throws IOException{

		io.write("Select City:");
		String cityName = io.read();
		Bson filterCity = null;
		Document sort = new Document();
		sort.append("nr_rooms",-1);
		sort.append("review_score",-1);

		FindIterable<Document> userIt = QueryHelper.getUser(collectionUsers, currentUser);
		Iterator userIterator = userIt.iterator();
		int topHotelsLimit = 20;
		if (userIterator.hasNext()) {
			Document user = (Document) userIterator.next();
			topHotelsLimit = user.getInteger("topHotelsLimit");
		}

		if(cityName!=null && !cityName.equals(""))
			filterCity= Filters.eq("city_hotel", cityName);

		AggregateIterable<Document> hotels = null;

		if(filterCity!=null){
			hotels = collectionHotels.aggregate(Arrays.asList(
					match(filterCity),
					sort(sort),
					limit(topHotelsLimit)
			));
		}else{
			hotels = collectionHotels.aggregate(Arrays.asList(
					sort(sort),
					limit(topHotelsLimit)
			));
		}

		showHotels(hotels);

	}

	private void showHotels(AggregateIterable<Document> hotels ){
		int numRows = 0;
		Iterator it = hotels.iterator();
		ArrayList<Hotel> hotelList = new ArrayList();
		while(it.hasNext()){
			numRows++;
			hotelList.add(createHotel((Document)it.next()));
		}

		if (numRows>0) {

			ui.showHotels(numRows,hotelList, collectionPois, collectionUsers, currentUser, false);
			io.write("\n\n");
		}
		else{
			io.write("----------------------------------------------------");
			io.write("Hotels not found. Please, try again");
			io.write("----------------------------------------------------");

		}
	}


	public HotelQuery prepareQueryHotel(boolean selectFilters) throws IOException {

		Document group = new Document();
		group.append("continent", "$continent_id");

		Document sort = new Document();
		sort.append("_id.continent", 1);


		AggregateIterable<Document> continents = collectionHotels.aggregate(Arrays.asList(
				group(group),
				sort(sort)
		));

		io.write("Select Continent: ");

		int numRows = 0;
		Iterator it = continents.iterator();
		ArrayList<String> theContinents = new ArrayList();
		while (it.hasNext()) {
			numRows++;
			Document doc = (Document) it.next();
			String continent = (String) ((Document) doc.get("_id")).get("continent");
			theContinents.add(continent);
		}

		io.write("-----------");

		StringBuffer cont = new StringBuffer();
		for (Document continent : continents) {
			Object continentName = (Object) ((Document)continent.get("_id")).get("continent");
			if(continentName!=null && !continentName.equals("")){
				cont.append(continentName);
				cont.append("\n");
			}

		}
		io.write(cont.toString());

		/*Object[][] data = new Object[numRows][];
		String columnNames[] = {"Continent"};
		int i = 0;
		for (Document continent : continents) {

			Object[] row = new Object[columnNames.length];
			row[0] = ((Document) continent.get("_id")).get("continent");
			data[i] = row;
			i++;
		}
		TextTable textTable = new TextTable(columnNames, data);
		textTable.printTable();
		*/

		String continentName = io.read();

		if(!theContinents.contains(continentName)){
			io.write("----------------------------------------------------");
			io.write("Incorrect Option. Please, try again");
			io.write("----------------------------------------------------");
			io.write("\n");
			return prepareQueryHotel(selectFilters);
		}

		group = new Document();
		group.append("country", "$cc1");

		Document match = new Document();
		match.append("continent_id", continentName);

		sort = new Document();
		sort.append("_id.country", 1);

		AggregateIterable<Document> countries = collectionHotels.aggregate(Arrays.asList(
				match(match),
				group(group),
				sort(sort)
		));

		io.write("\nSelect Country: ");

		numRows = 0;
		it = countries.iterator();
		ArrayList<String> theCountries = new ArrayList();
		while (it.hasNext()) {
			numRows++;
			Document doc = (Document) it.next();
			String country = (String) ((Document) doc.get("_id")).get("country");
			theCountries.add(country);
		}

		io.write("-----------");

		StringBuffer count = new StringBuffer();
		for (Document country : countries) {
			Object countryName = (Object) ((Document)country.get("_id")).get("country");
			count.append(countryName);
			count.append("\n");
		}
		io.write(count.toString());

		/*data = new Object[numRows][];
		String columnNames2[] = {"Country"};
		i = 0;
		for (Document country : countries) {

			Object[] row = new Object[columnNames2.length];
			row[0] = ((Document) country.get("_id")).get("country");
			data[i] = row;
			i++;
		}

		textTable = new TextTable(columnNames2, data);
		textTable.printTable();
		*/

		String countryName = io.read();

		if(!theCountries.contains(countryName)){
			io.write("----------------------------------------------------");
			io.write("Incorrect Option. Please, try again");
			io.write("----------------------------------------------------");
			io.write("\n");
			return prepareQueryHotel(selectFilters);
		}

		group = new Document();
		group.append("city", "$city_hotel");

		match = new Document();
		match.append("cc1", countryName);

		sort = new Document();
		sort.append("_id.city", 1);

		AggregateIterable<Document> cities = collectionHotels.aggregate(Arrays.asList(
				match(match),
				group(group),
				sort(sort)
		));

		io.write("\nSelect City: ");

		numRows = 0;
		it = cities.iterator();
		ArrayList<String> theCities = new ArrayList();
		while (it.hasNext()) {
			numRows++;
			Document doc = (Document) it.next();
			String city = (String) ((Document) doc.get("_id")).get("city");
			//System.out.println("City:" + city);
			theCities.add(city);
		}

		io.write("-----------");

		StringBuffer cit = new StringBuffer();
		for (Document city : cities) {
			Object cityName = (Object) ((Document)city.get("_id")).get("city");
			cit.append(cityName);
			cit.append("\n");
		}
		io.write(cit.toString());

		/*data = new Object[numRows][];
		String columnNames3[] = {"City"};
		i = 0;
		for (Document city : cities) {

			Object[] row = new Object[columnNames3.length];
			row[0] = ((Document) city.get("_id")).get("city");
			data[i] = row;
			i++;
		}

		textTable = new TextTable(columnNames3, data);
		textTable.printTable();
		*/

		String cityName = io.read();

		if(!theCities.contains(cityName)){
			io.write("----------------------------------------------------");
			io.write("Incorrect Option. Please, try again");
			io.write("----------------------------------------------------");
			io.write("\n");
			return prepareQueryHotel(selectFilters);
		}

		String score = null;
		String clazz = null;
		String maxRate = null;

		if(selectFilters) {
			io.write("\nSelect Score (0 - 10): ");
			score = io.read();
			while (!Util.isNumericBetween(score, 0, 10)){
				io.write("\nIncorrect value. Please, try again");
				io.write("\nSelect Score (0 - 10): ");
				score = io.read();
			}

			io.write("\nSelect Category (0 = hostel - 5 = 5*): ");
			clazz = io.read();
			while (!Util.isNumericBetween(clazz, 0, 5)){
				io.write("\nIncorrect value. Please, try again");
				io.write("\nSelect Category (0 = hostel - 5 = 5*): ");
				clazz = io.read();
			}
			io.write("\nSelect Max Rate (EUR): ");
			maxRate = io.read();
			while (!Util.isNumeric(maxRate)){
				io.write("\nIncorrect value. Please, try again");
				io.write("\nSelect Max Rate (EUR): ");
				maxRate = io.read();
			}
		}

		HotelQuery hotelQuery = new HotelQuery();
		hotelQuery.setContinent(continentName);
		hotelQuery.setCountry(countryName);
		hotelQuery.setCity(cityName);
		if(selectFilters) {
			hotelQuery.setClazz(clazz);
			hotelQuery.setMin_rate_EUR(maxRate);
			hotelQuery.setReview_score(score);
		}

		return hotelQuery;
	}

	public void searchHotel() throws IOException{

		HotelQuery hotelQuery = prepareQueryHotel(true);
		String cityName = hotelQuery.getCity();
		String score = hotelQuery.getReview_score();
		String clazz = hotelQuery.getClazz();
		String maxRate = hotelQuery.getMin_rate_EUR();

		Bson filterCity= Filters.eq("city_hotel", cityName);
		Bson filterScore=null;
		if (score.startsWith("+"))
			filterScore= Filters.gt("review_score", new Double(score.substring(1)));
		else if (score.endsWith("+"))
			filterScore= Filters.gte("review_score", new Double(score.substring(0,score.length()-1)));
		else if (score.startsWith("-"))
			filterScore= Filters.lt("review_score", new Double(score.substring(1)));
		else if (score.endsWith("-"))
			filterScore= Filters.lte("review_score", new Double(score.substring(0,score.length()-1)));
		else
			filterScore= Filters.eq("review_score", new Double(score));

		Bson filterClazz=null;
		if (clazz.startsWith("+"))
			filterClazz= Filters.gt("class", new Double(clazz.substring(1)));
		else if (clazz.endsWith("+"))
			filterClazz= Filters.gte("class", new Double(clazz.substring(0,clazz.length()-1)));
		else if (clazz.startsWith("-"))
			filterClazz= Filters.lt("class", new Double(clazz.substring(1)));
		else if (clazz.endsWith("-"))
			filterClazz= Filters.lte("class", new Double(clazz.substring(0,clazz.length()-1)));
		else
			filterClazz= Filters.eq("class", new Double(clazz));


		Bson filterMinRate = Filters.lte("min_rate_EUR", new Double(maxRate));
		Bson filters= Filters.and(filterCity, filterScore, filterClazz,filterMinRate);

		Document sort = new Document();
		sort.append("review_score",-1);
		sort.append("class",-1);
		sort.append("min_rate_EUR",-1);

		AggregateIterable<Document> hotels = collectionHotels.aggregate(Arrays.asList(
				match(filters),
				sort(sort)
		));

		int numRows = 0;
		Iterator it = hotels.iterator();
		ArrayList<Hotel> hotelList = new ArrayList();
		while(it.hasNext()){
			numRows++;
			hotelList.add(createHotel((Document)it.next()));
		}

		if (numRows>0) {

			ui.showHotels(numRows,hotelList, collectionPois, collectionUsers, currentUser, false);
			io.write("\n\n");
		}
		else{
			io.write("----------------------------------------------------");
			io.write("Hotel not found. Please, try again");
			io.write("----------------------------------------------------");

		}

	}

	public void searchHotelFeatures() throws IOException {
		String field = "desc_es";

		FindIterable<Document> userIt = QueryHelper.getUser(collectionUsers, currentUser);
		Iterator userIterator = userIt.iterator();
		String userLang = "es";

		if (userIterator.hasNext()) {
			Document user = (Document) userIterator.next();
			userLang = user.getString("language");
		}

		if("en".equals(userLang))
			field = "desc_en";

		advancedSearchHotels(field);
	}

	public void searchHotelComments() throws IOException {
		String field = "reviews.comment";
		advancedSearchHotels(field);
	}

	public void searchSimilarHotels() throws IOException {
		//HotelQuery hotelQuery = prepareQueryHotel(false);
		//String continentName = hotelQuery.getContinent();
		//String countryName = hotelQuery.getCountry();
		//String cityName = hotelQuery.getCity();
		io.write("Hotel name: ");
		String hotelName = io.read();
		int id = searchHotelByName(hotelName);

		if (id!=-1) {

			Document hotelFilter = new Document();
			hotelFilter.append("id", id);
			//hotelFilter.append("city_hotel", cityName);

			FindIterable<Document> doc = collectionHotels.find(hotelFilter);

			if (doc != null && doc.iterator().hasNext()) {
				Document hotel = doc.iterator().next();
				double review_score = hotel.getDouble("review_score");
				int step_score = 1;
				double clazz = hotel.getDouble("class");
				int step_clazz = 1;
				double maxRate = hotel.getDouble("min_rate_EUR");
				int step_maxRate = 25;
				int minimumShouldMatch = 2;
				int maxRecommendations = 20;

				String continentName = hotel.getString("continent_id");
				String countryName = hotel.getString("cc1");
				String cityName = hotel.getString("city_hotel");
				hotelName = hotel.getString("name");

				SearchResponse response = ElasticSearchQueryHelper.moreLikeThisQuery(continentName, countryName, cityName, hotelName, review_score, step_score, clazz, step_clazz, maxRate, step_maxRate, minimumShouldMatch, maxRecommendations, "hotels", SearchType.DFS_QUERY_THEN_FETCH);

				ArrayList hotels = new ArrayList<Hotel>();
				for (SearchHit searchHit : response.getHits()) {
					hotels.add(createHotel(searchHit));
					//System.out.println("Score: " + searchHit.score());
				}

				if (hotels.size() > 0) {
					ui.showHotels(hotels.size(), hotels, collectionPois, collectionUsers, currentUser, false);
				} else {
					io.write("----------------------------------------------------");
					io.write("Similar hotels not found. Please, try again");
					io.write("----------------------------------------------------");
				}
			} else {
				io.write("----------------------------------------------------");
				io.write("Similar hotels not found. Please, try again");
				io.write("----------------------------------------------------");
			}
		}
	}


	public void advancedSearchHotels(String field) throws IOException {

		HotelQuery hotelQuery = prepareQueryHotel(true);
		String continentName = hotelQuery.getContinent();
		String countryName = hotelQuery.getCountry();
		String cityName = hotelQuery.getCity();
		String score = hotelQuery.getReview_score();
		String clazz = hotelQuery.getClazz();
		String maxRate = hotelQuery.getMin_rate_EUR();

		io.write("\nSelect Features/Terms: ");
		String features = io.read();
		SearchResponse response =  ElasticSearchQueryHelper.advancedSearchHotelsQuery(continentName, countryName, cityName, score, clazz, maxRate,"hotels", SearchType.DFS_QUERY_THEN_FETCH, field, features, 100);

		ArrayList hotels = new ArrayList<Hotel>();
		for (SearchHit searchHit : response.getHits()){
			hotels.add(createHotel(searchHit));
		}

		if (hotels.size()>0){
			ui.showHotels(hotels.size(),hotels, collectionPois, collectionUsers, currentUser, false);
		}
		else{
			io.write("----------------------------------------------------");
			io.write("Hotels not found. Please, try again");
			io.write("----------------------------------------------------");
		}

	}


	public void searchHotelFeaturesTest() throws IOException{

		SearchResponse response =  ElasticSearchQueryHelper.rangeQuery("hotels", SearchType.DFS_QUERY_THEN_FETCH, "min_rate_EUR", 200, 200);

		ArrayList hotels = new ArrayList<Hotel>();
		for (SearchHit searchHit : response.getHits()){
			hotels.add(createHotel(searchHit));
		}

		String field = "desc_es";
		String text="jardines piscina cubierta";
		response =  ElasticSearchQueryHelper.matchPhraseQuery("hotels", SearchType.DFS_QUERY_THEN_FETCH, field, text,3);

		hotels = new ArrayList<Hotel>();
		for (SearchHit searchHit : response.getHits()){
			hotels.add(createHotel(searchHit));
		}


		field = "desc_es";
		text="piscina sauna terraza";
		response =  ElasticSearchQueryHelper.matchPhraseQuery("hotels", SearchType.DFS_QUERY_THEN_FETCH, field, text,100);

		hotels = new ArrayList<Hotel>();
		for (SearchHit searchHit : response.getHits()){
			hotels.add(createHotel(searchHit));
		}

		if (hotels.size()>0){
			ui.showHotels(hotels.size(),hotels, collectionPois, collectionUsers, currentUser, false);
		}

		/*field = "reviews.comment";
		text="Bastante buen hotel";

		QueryBuilder qb = QueryBuilders.matchPhraseQuery(field, text).slop(100);

		TransportClient client = new ElasticSearch().getClient();

		SearchResponse scrollResp = client.prepareSearch()
				.addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
				.setScroll(new TimeValue(60000))
				.setQuery(qb)
				.setSize(100).get(); //max of 100 hits will be returned for each scroll
		//Scroll until no hits are returned
		hotels = new ArrayList<Hotel>();
		do {
			for (SearchHit hit : scrollResp.getHits().getHits()) {
					hotels.add(createHotel(hit));

			}

			scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
		} while(scrollResp.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while loop.

		if (hotels.size()>0){
			ui.showHotels2(hotels.size(),hotels, collectionPois, collectionUsers, currentUser, false);
		}
		*/

	}

	public void searchHotelPois() throws IOException{

		io.write("Hotel name: ");
		String hotelName = io.read();
		int id =  searchHotelByName(hotelName);

		if(id!=-1) {

			Document hotelFilter = new Document();
			hotelFilter.append("id", id);
			FindIterable<Document> doc = collectionHotels.find(hotelFilter);

		/*
		Document hotelFilter = new Document();
		hotelFilter.append("name",hotelName);
		FindIterable<Document> doc = collectionHotels.find(hotelFilter);
		*/

			if (doc != null && doc.iterator().hasNext()) {

				Document hotel = doc.iterator().next();

				ArrayList<Double> coords = (ArrayList) hotel.get("coord");
				double longitude = coords.get(0);
				double latitude = coords.get(1);


				FindIterable<Document> userIt = QueryHelper.getUser(collectionUsers, currentUser);
				Iterator userIterator = userIt.iterator();
				double minDistance = 0d;
				double maxDistance = 1000d;
				int userPoisLimit = 10;
				String[] poisTypes = null;
				if (userIterator.hasNext()) {
					Document user = (Document) userIterator.next();
					minDistance = user.getDouble("poisMinDistance");
					maxDistance = user.getDouble("poisMaxDistance");
					userPoisLimit = user.getInteger("poisNumberLimit");
					poisTypes = user.getString("poisTypes").split(",");
				}

				ArrayList<Object> valuesFilter = new ArrayList();

				if (poisTypes.length == 1 && poisTypes[0].equals("0")) {
					//All POIs
					Iterator it = PoisTypes.poisTypes.values().iterator();
					while (it.hasNext()) {
						valuesFilter.add(it.next());
					}

				} else {
					for (int j = 0; j < poisTypes.length; j++) {
						String poiType = PoisTypes.poisTypes.get(new Integer(poisTypes[j]));
						valuesFilter.add(poiType);
					}
				}

				//String keyFilter = "type";
				//String filterOperatorType = "or";

				//db.pois.createIndex({"coord":"2dsphere"})

				//db.pois.find( {"coord":{ $nearSphere:{ $geometry: { type: "Point", coordinates: [ -73.9667, 40.78 ] }, $minDistance: 0, $maxDistance: 5000 }}})

				//MongoCursor<Document> pois = QueryHelper.findByDistanceAndFilters(collectionPois,longitude,latitude,minDistance, maxDistance,keyFilter,valuesFilter,filterOperatorType);
				//io.write("----------------------------------------------------");
				//int numPois=0;
				//ArrayList<Document> poiList = new ArrayList();
				//while (pois.hasNext() && numPois<userPoisLimit)
				//{
				//Document poi = pois.next();
				//	String id = poi.get("_id").toString();
				//	String name=poi.getString("name");
				//	String type=poi.getString("type");
				//	coords = (ArrayList) poi.get("coord");
				//	longitude = coords.get(0);
				//	latitude = coords.get(1);
				//	numPois++;
				//	io.write("[Type: " + type + "] [Name: " + name + "][Coord: " + "[" + longitude + "," + latitude + "]"+ "\n");
				//	poiList.add(poi);
				//}
				//io.write("----------------------------------------------------");
				//

				Document pois = QueryHelper.findByDistance("pois", longitude, latitude, minDistance, maxDistance);
				io.write("----------------------------------------------------");

				ArrayList results = (ArrayList) pois.get("results");

				int numPois = 0;
				ArrayList<Document> poiList = new ArrayList();

				for (int i = 0; i < results.size(); i++) {
					Double distance = ((Document) results.get(i)).getDouble("dis");
					distance = distance * 6371 * 1000; //Transform radians (returned by $geoNear) to meters
					Document poi = (Document) ((Document) results.get(i)).get("obj");
					String poiType = poi.getString("poi_type");
					String name = poi.getString("poi_name");
					if (valuesFilter.contains(poiType)) {
						numPois++;
						poi.append("distance", distance);
						io.write("[Type: " + poiType + "] [Name: " + name + "][Coord: " + "[" + longitude + "," + latitude + "]" + "[Distance (m): " + distance + "]\n");
						poiList.add(poi);
					}
					if (numPois == userPoisLimit)
						break;
				}
				io.write("----------------------------------------------------");

				if (numPois > 0) {

					ui.showPois(numPois, poiList, hotelName);
					io.write("\n\n");
				} else {
					io.write("----------------------------------------------------");
					io.write("POIs not found for hotel " + hotelName + ". Please, try again");
					io.write("----------------------------------------------------");

				}
			} else {
				io.write("----------------------------------------------------");
				io.write("Hotel " + hotelName + " not found. Please, try again");
				io.write("----------------------------------------------------");

			}
		}
	}

	public void searchPoiHotels() throws IOException{

		io.write("POI name: ");
		String poiName = io.read();
		String id =  searchPoiByName(poiName);

		if(!"".equals(id)){

			Document poiFilter = new Document();
			poiFilter.append("poi_osm_id",id);

			FindIterable<Document> doc = collectionPois.find(poiFilter);

			if (doc!=null && doc.iterator().hasNext()) {

				Document poi = doc.iterator().next();

				ArrayList<Double> coords = (ArrayList) poi.get("poi_coord");
				double longitude = coords.get(0);
				double latitude = coords.get(1);

				FindIterable<Document> userIt = QueryHelper.getUser(collectionUsers, currentUser);
				Iterator userIterator = userIt.iterator();
				double minDistance = 0d;
				double maxDistance = 1000d;

				if (userIterator.hasNext()) {
					Document user = (Document) userIterator.next();
					minDistance = user.getDouble("poisMinDistance");
					maxDistance = user.getDouble("poisMaxDistance");
				}

				//db.hotels.createIndex({"coord":"2dsphere"})

				//db.hotels.find( {"coord":{ $nearSphere:{ $geometry: { type: "Point", coordinates: [ -73.9667, 40.78 ] }, $minDistance: 0, $maxDistance: 5000 }}})

				/*MongoCursor<Document> hotels = QueryHelper.findByDistanceAndFilters(collectionHotels,longitude,latitude,minDistance, maxDistance,null,null,null);

				int numRows = 0;
				ArrayList<Document> hotelList = new ArrayList();
				while(hotels.hasNext()){
					numRows++;
					hotelList.add(hotels.next());
				}*/

				Document hotels = QueryHelper.findByDistance("hotels", longitude, latitude, minDistance, maxDistance);

				ArrayList results = (ArrayList) hotels.get("results");

				int numRows = 0;
				ArrayList<Hotel> hotelList = new ArrayList();

				for (int i = 0; i < results.size(); i++) {
					Double distance = ((Document) results.get(i)).getDouble("dis");
					distance = distance * 6371 * 1000; //Transform radians (returned by $geoNear) to meters
					Document hotel = (Document) ((Document) results.get(i)).get("obj");
					hotel.append("distance", distance);
					hotelList.add(createHotel(hotel));
					numRows++;
				}

				if (numRows > 0)
					ui.showHotels(numRows, hotelList, collectionPois, collectionUsers, currentUser, true);
				else {
					io.write("-------------------------------------------------------");
					io.write("Hotels near " + poiName + " not found. Please, try again");
					io.write("-------------------------------------------------------");
				}
			}
		}
		else{
			io.write("----------------------------------------------------");
			io.write("POI " + poiName + " not found. Please, try again");
			io.write("----------------------------------------------------");

		}
	}

	public void showUserReviews() throws IOException{

		FindIterable<Document> userIt = QueryHelper.getUser(collectionUsers, currentUser);
		Iterator userIterator = userIt.iterator();

		if (userIterator.hasNext()) {
			Document user = (Document) userIterator.next();
			ArrayList<Document> reviewList = (ArrayList) user.get("reviews");
			if (reviewList == null || reviewList.size() == 0)
				reviewList = new ArrayList();

			if (reviewList.size()>0) {

				ArrayList<Document> reviewListSorted = new ArrayList();
				for (int i=reviewList.size()-1;i>=0;i--){
					reviewListSorted.add(reviewList.get(i));
				}

				ui.showPersonalHotelReviews(reviewListSorted.size(), reviewListSorted, collectionHotels, collectionUsers, currentUser);
			}
			else {
				io.write("-------------------------------------------------------");
				io.write("There aren't any reviews for user: " + currentUser);
				io.write("-------------------------------------------------------");
			}

		}

	}

	private int searchHotelByName(String hotelName) throws IOException{

		int ident = -1;

		SearchResponse response =  ElasticSearchQueryHelper.elementQuery(hotelName, "name","hotels", SearchType.DFS_QUERY_THEN_FETCH,500);
		//SearchResponse response =  ElasticSearchQueryHelper.fuzzyQuery(hotelName, "name","hotels", SearchType.DFS_QUERY_THEN_FETCH,500, 2);

		ArrayList hotels = new ArrayList<Hotel>();
		for (SearchHit searchHit : response.getHits()){
			hotels.add(createHotel(searchHit));
		}

		if (hotels.size()>0) {

			io.write("\nFound " + hotels.size() + " hotels:");

			Object[][] data = new Object[hotels.size()][];
			String columnNames[] = {"Hotels"};
			int h = 0;
			for (Object hot : hotels) {
				Hotel hotel = (Hotel) hot;
				Object[] row = new Object[columnNames.length];
				int num = h + 1;
				row[0] = num + " -> " + hotel.getName() + "(" + hotel.getCity() + ")";
				data[h] = row;
				h++;
			}

			TextTable textTable = new TextTable(columnNames, data);
			textTable.printTable();

			io.write("\nSelect Hotel Id: ");
			String id = io.read();
			while (!Util.isNumericBetween(id, 1, hotels.size())) {
				io.write("\nIncorrect value. Please, try again");
				io.write("\nSelect Hotel Id: ");
				id = io.read();
			}

			ident = ((Hotel) hotels.get(Integer.parseInt(id) - 1)).getId();
		}
		else{
			io.write("----------------------------------------------------");
			io.write("Hotel " + hotelName + " not found. Please, try again");
			io.write("----------------------------------------------------");

		}

		return ident;

	}

	private String searchPoiByName(String poiName) throws IOException{

		String ident = "";

		SearchResponse response =  ElasticSearchQueryHelper.elementQuery(poiName, "poi_name","pois", SearchType.DFS_QUERY_THEN_FETCH,50);
		//SearchResponse response =  ElasticSearchQueryHelper.fuzzyQuery(poiName, "poi_name","pois", SearchType.DFS_QUERY_THEN_FETCH,50,2);

		ArrayList pois = new ArrayList<Poi>();
		for (SearchHit searchHit : response.getHits()){
			pois.add(createPoi(searchHit));
		}

		if (pois.size()>0) {

			io.write("\nFound " + pois.size() + " pois:");

			Object[][] data = new Object[pois.size()][];
			String columnNames[] = {"POIs"};
			int h = 0;
			for (Object obj : pois) {
				Poi poi = (Poi) obj;
				Object[] row = new Object[columnNames.length];
				int num = h + 1;
				row[0] = num + " -> " + poi.getPoi_name() + "(" + poi.getPoi_type() + ")";
				data[h] = row;
				h++;
			}

			TextTable textTable = new TextTable(columnNames, data);
			textTable.printTable();

			io.write("\nSelect Poi Id: ");
			String id = io.read();
			while (!Util.isNumericBetween(id, 1, pois.size())) {
				io.write("\nIncorrect value. Please, try again");
				io.write("\nSelect Poi Id: ");
				id = io.read();
			}

			ident = ((Poi) pois.get(Integer.parseInt(id) - 1)).getPois_osm_id();
		}
		else{
			io.write("----------------------------------------------------");
			io.write("Poi " + poiName + " not found. Please, try again");
			io.write("----------------------------------------------------");

		}

		return ident;

	}

	public void reviewHotel() throws IOException{

		io.write("Hotel name: ");
		String hotelName = io.read();
		int id = searchHotelByName(hotelName);

		if (id!=-1) {
			Document hotelFilter = new Document();
			hotelFilter.append("id", id);
			FindIterable<Document> doc = collectionHotels.find(hotelFilter);

			if (doc != null && doc.iterator().hasNext()) {

				Document hotel = doc.iterator().next();

				int hotelId = hotel.getInteger("id");

				io.write("\nScore Location (0-10): ");
				String loc = io.read();
				while (!Util.isNumericBetween(loc, 0, 10)) {
					io.write("\nIncorrect value. Please, try again");
					io.write("\nScore Location (0-10): ");
					loc = io.read();
				}
				Double location = Double.parseDouble(loc);

				io.write("\nScore Cleanliness (0-10): ");
				String clean = io.read();
				while (!Util.isNumericBetween(clean, 0, 10)) {
					io.write("\nIncorrect value. Please, try again");
					io.write("\nScore Cleanliness (0-10): ");
					clean = io.read();
				}
				Double cleanliness = Double.parseDouble(clean);

				io.write("\nScore Confort (0-10): ");
				String conf = io.read();
				while (!Util.isNumericBetween(conf, 0, 10)) {
					io.write("\nIncorrect value. Please, try again");
					io.write("\nScore Confort (0-10): ");
					conf = io.read();
				}
				Double confort = Double.parseDouble(conf);

				io.write("\nScore Installations (0-10): ");
				String install = io.read();
				while (!Util.isNumericBetween(install, 0, 10)) {
					io.write("\nIncorrect value. Please, try again");
					io.write("\nScore Installations (0-10): ");
					install = io.read();
				}
				Double installations = Double.parseDouble(install);

				io.write("\nScore Personal (0-10): ");
				String pers = io.read();
				while (!Util.isNumericBetween(pers, 0, 10)) {
					io.write("\nIncorrect value. Please, try again");
					io.write("\nScore Personal (0-10): ");
					pers = io.read();
				}
				Double personal = Double.parseDouble(pers);

				io.write("\nScore WIFI (0-10): ");
				String wif = io.read();
				while (!Util.isNumericBetween(wif, 0, 10)) {
					io.write("\nIncorrect value. Please, try again");
					io.write("\nScore WIFI (0-10): ");
					wif = io.read();
				}
				Double wifi = Double.parseDouble(wif);

				io.write("\nScore Value for Money (0-10): ");
				String value = io.read();
				while (!Util.isNumericBetween(value, 0, 10)) {
					io.write("\nIncorrect value. Please, try again");
					io.write("\nScore Value for Money (0-10): ");
					value = io.read();
				}
				Double valueMoney = Double.parseDouble(value);

				double totalScore = Maths.round((location + cleanliness + confort + installations + personal + wifi + valueMoney) / 7, 2);

				io.write("\nComment: ");
				String comment = io.read();

				long date = System.currentTimeMillis();

				//Update User reviews
				FindIterable<Document> userIt = QueryHelper.getUser(collectionUsers, currentUser);
				Iterator userIterator = userIt.iterator();
				String userLang = "en";
				int age = 0;

				if (userIterator.hasNext()) {
					Document user = (Document) userIterator.next();
					ArrayList<Document> userReviews = (ArrayList) user.get("reviews");
					if (userReviews == null || userReviews.size() == 0)
						userReviews = new ArrayList();

					userLang = user.getString("language");
					if(user.getInteger("age")!=null)
						age = user.getInteger("age");

					Document review = new Document();
					review.append("hotel_id", hotelId);
					review.append("hotel_name", hotel.getString("name"));
					review.append("age",age);
					review.append("location", location);
					review.append("cleanliness", cleanliness);
					review.append("confort", confort);
					review.append("installations", installations);
					review.append("personal", personal);
					review.append("wifi", wifi);
					review.append("valueMoney", valueMoney);
					review.append("lang", userLang);
					review.append("comment", comment);
					review.append("score", totalScore);
					review.append("date", date);

					userReviews.add(review);

					user.append("reviews", userReviews);

					Document userToUpdate = DbUtils.createDocument("username", currentUser);

					QueryHelper.updateUser(collectionUsers, userToUpdate, user);

				}

				//Update Hotel reviews

				ArrayList<Document> hotelReviews = (ArrayList) hotel.get("reviews");

				if (hotelReviews == null || hotelReviews.size() == 0)
					hotelReviews = new ArrayList();

				Document review = new Document();

				review.append("username", currentUser);
				if (age > 0)
					review.append("age",age);
				review.append("location", location);
				review.append("cleanliness", cleanliness);
				review.append("confort", confort);
				review.append("installations", installations);
				review.append("personal", personal);
				review.append("wifi", wifi);
				review.append("valueMoney", valueMoney);
				review.append("lang", userLang);
				review.append("comment", comment);
				review.append("score", totalScore);
				review.append("date", date);

				hotelReviews.add(review);

				double oldScore = hotel.getDouble("review_score");
				int oldNumReviews = hotel.getInteger("review_nr");
				int newNumReviews = oldNumReviews + 1;
				double newScore = ((oldScore * oldNumReviews) + totalScore) / newNumReviews;

				hotel.append("reviews", hotelReviews);
				hotel.append("review_score", newScore);
				hotel.append("review_nr", newNumReviews);

				Document hotelToUpdate = DbUtils.createDocument("id", hotelId);

				QueryHelper.updateHotel(collectionHotels, hotelToUpdate, hotel);


			} else {
				io.write("----------------------------------------------------");
				io.write("Hotel " + hotelName + " not found. Please, try again");
				io.write("----------------------------------------------------");

			}
		}
	}

	public void modifyUserPreferences() throws IOException {

		FindIterable<Document> userIt = QueryHelper.getUser(collectionUsers, currentUser);
		Iterator userIterator = userIt.iterator();
		double poisMinDistance = 0d;
		double poisMaxDistance = 1000d;
		int poisNumberLimit = 10;
		int topHotelsLimit = 20;
		String[] poisTypes = null;
		String language = "es";
		String password="";
		String name="";
		String surname="";
		int age = 0;
		String email="";
		if (userIterator.hasNext()) {
			Document user = (Document) userIterator.next();
			password = user.getString("password");
			name = user.getString("name");
			surname = user.getString("surname");
			age = (user.getInteger("age")!=null)?user.getInteger("age"):0;
			email = user.getString("email");
			language = user.getString("language");

			poisMinDistance = user.getDouble("poisMinDistance");
			poisMaxDistance = user.getDouble("poisMaxDistance");
			poisNumberLimit = user.getInteger("poisNumberLimit");
			poisTypes = user.getString("poisTypes").split(",");
			topHotelsLimit = user.getInteger("topHotelsLimit");

			io.write("New Password " + "[" + password + "]: ");
			password = io.read();

			io.write("\nNew Name " + "[" + name + "]: ");
			name = io.read();

			io.write("\nNew Surname " + "[" + surname + "]: ");
			surname = io.read();

			io.write("\nNew Age " + "[" + age + "]: ");
			String newAge = io.read();
			while (!Util.isNumeric(newAge)){
				io.write("\nIncorrect value.  Please, try again");
				io.write("\nNew Age " + "[" + newAge + "]: ");
				newAge = io.read();
			}

			io.write("\nNew Email " + "[" + email + "]: ");
			email = io.read();

			io.write("\nNew Language " + "[" + language + "]: ");
			language = io.read();

			while (!"es".equals(language) && !"en".equals(language)){
				io.write("\nIncorrect value. Valid options: es (Spanish) or en (English)");
				io.write("\nNew Language " + "[" + language + "]: ");
				language = io.read();
			}

			io.write("\nNew Pois Min Distance (m) " + "[" + poisMinDistance + "]: ");
			String poisMinDist = io.read();
			while (!Util.isNumeric(poisMinDist)){
				io.write("\nIncorrect value.  Please, try again");
				io.write("\nNew Pois Min Distance (m) " + "[" + poisMinDistance + "]: ");
				poisMinDist = io.read();
			}

			io.write("\nNew Pois Max Distance (m) " + "[" + poisMaxDistance + "]: ");
			String poisMaxDist = io.read();
			while (!Util.isNumeric(poisMaxDist)){
				io.write("\nIncorrect value.  Please, try again");
				io.write("\nNew Pois Max Distance (m) " + "[" + poisMaxDistance + "]: ");
				poisMaxDist = io.read();
			}


			io.write("\nNew Pois Number Limit " + "[" + poisNumberLimit + "]: ");
			String poisNumLimit = io.read();
			while (!Util.isNumeric(poisNumLimit)){
				io.write("\nIncorrect value.  Please, try again");
				io.write("\nNew Pois Number Limit" + "[" + poisNumberLimit + "]: ");
				poisNumLimit = io.read();
			}

			StringBuffer sb = new StringBuffer();
			for(String poiType : poisTypes){
				sb.append(poiType);
				sb.append(",");
			}
			sb = sb.deleteCharAt(sb.length()-1);

			io.write("\nNew Pois Types " + "[" + sb.toString() + "]: ");
			io.write(PoisTypes.pois);
			String poisTyp = io.read();
			while (!Util.areValidTypes(poisTyp)){
				io.write("\nIncorrect value.  Please, try again");
				io.write("\nNew Pois Types " + "[" + sb.toString() + "]: ");
				io.write(PoisTypes.pois);
				poisTyp = io.read();
			}

			io.write("\nNew TOP Hotels Limit " + "[" + topHotelsLimit + "]: ");
			String topHotelsLim = io.read();
			while (!Util.isNumeric(topHotelsLim)){
				io.write("\nIncorrect value.  Please, try again");
				io.write("\nNew TOP Hotels Limit " + "[" + topHotelsLimit + "]: ");
				topHotelsLim = io.read();
			}

			//Update User Preferences
			user.put("password", password);
			user.put("name", name);
			user.put("surname", surname);
			user.put("age", Integer.parseInt(newAge));
			user.put("email", email);
			user.put("language", language);
			user.put("poisMinDistance", Double.parseDouble(poisMinDist));
			user.put("poisMaxDistance", Double.parseDouble(poisMaxDist));
			user.put("poisNumberLimit", Integer.parseInt(poisNumLimit));
			user.put("poisTypes", poisTyp);
			user.put("topHotelsLimit", Integer.parseInt(topHotelsLim));


			try {
				Document userToUpdate = DbUtils.createDocument("username", currentUser);
				QueryHelper.updateUser(collectionUsers, userToUpdate, user);
				io.write("\nUser: " + currentUser + " has been updated succesfully");
			} catch (Exception e) {
				io.write("\nThere has been a problem updating user: " + currentUser + ". Please, try again");
			}

		}

	}

	private Hotel createHotel(Document doc){
		Hotel hotel = new Hotel();

		try {

			Integer id = (Integer) doc.getInteger("id");
			if (id != null)
				hotel.setId(id);

			Double distance = (Double) doc.getDouble("distance");
			if (distance != null)
				hotel.setDistance(distance);

			String name = (String) doc.getString("name");
			if (name != null)
				hotel.setName(name);

			Double min_rate_EUR = (Double) doc.getDouble("min_rate_EUR");
			if (min_rate_EUR != null)
				hotel.setMin_rate_EUR(min_rate_EUR);

			Double max_rate_EUR = (Double) doc.getDouble("max_rate_EUR");
			if (max_rate_EUR != null)
				hotel.setMax_rate_EUR(max_rate_EUR);

			String city_hotel = (String) doc.getString("city_hotel");
			if (city_hotel != null)
				hotel.setCity(city_hotel);

			String cc1 = (String) doc.getString("cc1");
			if (cc1 != null)
				hotel.setCountry(cc1);

			String continent = (String) doc.getString("continent_id");
			if (continent != null)
				hotel.setContinent(continent);

			String zip = (String) doc.getString("zip");
			if (zip != null)
				hotel.setZip(zip);

			Integer ufi = (Integer) doc.getInteger("ufi");
			if (ufi != null)
				hotel.setUfi(ufi);

			String currencyCode = (String) doc.getString("currencyCode");
			if (currencyCode != null)
				hotel.setCurrencyCode(currencyCode);

			String city_unique = (String) doc.getString("city_unique");
			if (city_unique != null)
				hotel.setCity_unique(city_unique);

			String city_preferred = (String) doc.getString("city_preferred");
			if (city_preferred != null)
				hotel.setCity_preferred(city_preferred);

			String photo_url = (String) doc.getString("photo_url");
			if (photo_url != null)
				hotel.setPhoto_url(photo_url);

			String hotel_url = (String) doc.getString("hotel_url");
			if (hotel_url != null)
				hotel.setHotel_url(hotel_url);

			Double review_score = (Double) doc.getDouble("review_score");
			if (review_score != null)
				hotel.setReview_score(review_score);

			Integer review_nr = (Integer) doc.getInteger("review_nr");
			if (review_nr != null)
				hotel.setReview_nr(review_nr);

			Double clazz = (Double) doc.getDouble("class");
			if (clazz != null)
				hotel.setClazz(clazz);

			Integer nr_rooms = (Integer) doc.getInteger("nr_rooms");
			if (nr_rooms != null)
				hotel.setNr_rooms(nr_rooms);

			String desc_es = (String) doc.getString("desc_es");
			if (desc_es != null)
				hotel.setDesc_es(desc_es);

			String desc_en = (String) doc.getString("desc_en");
			if (desc_en != null)
				hotel.setDesc_en(desc_en);

			ArrayList coord = (ArrayList) doc.get("coord");
			if (coord != null)
				hotel.setCoord(coord);
		}
		catch (Exception e){
			e.printStackTrace();
		}

		return hotel;

	}

	private Hotel createHotel(SearchHit searchHit){
		Hotel hotel = new Hotel();

		try {

			Integer id = (Integer) searchHit.getSource().get("id");
			if (id != null)
				hotel.setId(id);

			String name = (String) searchHit.getSource().get("name");
			if (name != null)
				hotel.setName(name);

			Double min_rate_EUR = (Double) searchHit.getSource().get("min_rate_EUR");
			if (min_rate_EUR != null)
				hotel.setMin_rate_EUR(min_rate_EUR);

			Double max_rate_EUR = (Double) searchHit.getSource().get("max_rate_EUR");
			if (max_rate_EUR != null)
				hotel.setMax_rate_EUR(max_rate_EUR);

			String city_hotel = (String) searchHit.getSource().get("city_hotel");
			if (city_hotel != null)
				hotel.setCity(city_hotel);

			String cc1 = (String) searchHit.getSource().get("cc1");
			if (cc1 != null)
				hotel.setCountry(cc1);

			String continent = (String) searchHit.getSource().get("continent_id");
			if (continent != null)
				hotel.setContinent(continent);

			String zip = (String) searchHit.getSource().get("zip");
			if (zip != null)
				hotel.setZip(zip);

			Integer ufi = (Integer) searchHit.getSource().get("ufi");
			if (ufi != null)
				hotel.setUfi(ufi);

			String currencyCode = (String) searchHit.getSource().get("currencyCode");
			if (currencyCode != null)
				hotel.setCurrencyCode(currencyCode);

			String city_unique = (String) searchHit.getSource().get("city_unique");
			if (city_unique != null)
				hotel.setCity_unique(city_unique);

			String city_preferred = (String) searchHit.getSource().get("city_preferred");
			if (city_preferred != null)
				hotel.setCity_preferred(city_preferred);

			String photo_url = (String) searchHit.getSource().get("photo_url");
			if (photo_url != null)
				hotel.setPhoto_url(photo_url);

			String hotel_url = (String) searchHit.getSource().get("hotel_url");
			if (hotel_url != null)
				hotel.setHotel_url(hotel_url);

			Double review_score = (Double) searchHit.getSource().get("review_score");
			if (review_score != null)
				hotel.setReview_score(review_score);

			Integer review_nr = (Integer) searchHit.getSource().get("review_nr");
			if (review_nr != null)
				hotel.setReview_nr(review_nr);

			Double clazz = (Double) searchHit.getSource().get("class");
			if (clazz != null)
				hotel.setClazz(clazz);

			Integer nr_rooms = (Integer) searchHit.getSource().get("nr_rooms");
			if (nr_rooms != null)
				hotel.setNr_rooms(nr_rooms);

			String desc_es = (String) searchHit.getSource().get("desc_es");
			if (desc_es != null)
				hotel.setDesc_es(desc_es);

			String desc_en = (String) searchHit.getSource().get("desc_en");
			if (desc_en != null)
				hotel.setDesc_en(desc_en);

			ArrayList coord = (ArrayList) searchHit.getSource().get("coord");
			if (coord != null)
				hotel.setCoord(coord);
		}
		catch (Exception e){
			e.printStackTrace();
		}

		return hotel;
	}

	private Poi createPoi(SearchHit searchHit){
		Poi poi = new Poi();

		try {

			String id = (String) searchHit.getSource().get("poi_osm_id");
			if (id != null)
				poi.setPois_osm_id(id);

			String name = (String) searchHit.getSource().get("poi_name");
			if (name != null)
				poi.setPoi_name(name);

			String type = (String) searchHit.getSource().get("poi_type");
			if (name != null)
				poi.setPoi_type(type);


			ArrayList coord = (ArrayList) searchHit.getSource().get("coord");
			if (coord != null)
				poi.setCoord(coord);
		}
		catch (Exception e){
			e.printStackTrace();
		}

		return poi;
	}
}
