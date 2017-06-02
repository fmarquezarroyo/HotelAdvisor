package es.pfm.hoteladvisor.utils;

import java.util.HashMap;

public class PoisTypes {

    public static final HashMap<Integer,String> poisTypes;
    static
    {
        poisTypes = new HashMap<Integer, String>();
        poisTypes.put(1, "ACCOMMO_ALPINEHUT");
        poisTypes.put(2, "ACCOMMO_CAMPING");
        poisTypes.put(3, "ACCOMMO_CARAVAN");
        poisTypes.put(4, "ACCOMMO_CHALET");
        poisTypes.put(5, "ACCOMMO_HOSTEL");
        poisTypes.put(6, "ACCOMMO_HOTEL");
        poisTypes.put(7, "ACCOMMO_MOTEL");
        poisTypes.put(8, "AMENITY_COURT");
        poisTypes.put(9, "AMENITY_FIRESTATION");
        poisTypes.put(11, "AMENITY_LIBRARY");
        poisTypes.put(12, "AMENITY_PLAYGROUND");
        poisTypes.put(13, "AMENITY_POLICE");
        poisTypes.put(14, "AMENITY_POSTOFFICE");
        poisTypes.put(15, "AMENITY_PRISON");
        poisTypes.put(16, "AMENITY_PUBLICBUILDING");
        poisTypes.put(17, "AMENITY_TOWNHALL");
        poisTypes.put(18, "BARRIER_BLOCKS");
        poisTypes.put(19, "EDUCATION_COLLEGE");
        poisTypes.put(20, "EDUCATION_NURSERY");
        poisTypes.put(21, "EDUCATION_SCHOOL");
        poisTypes.put(22, "EDUCATION_UNIVERSITY");
        poisTypes.put(23, "FOOD_BAR");
        poisTypes.put(24, "FOOD_BIERGARTEN");
        poisTypes.put(25, "FOOD_CAFE");
        poisTypes.put(26, "FOOD_FASTFOOD");
        poisTypes.put(27, "FOOD_ICECREAM");
        poisTypes.put(28, "FOOD_PUB");
        poisTypes.put(29, "FOOD_RESTAURANT");
        poisTypes.put(30, "HEALTH_DENTIST");
        poisTypes.put(31, "HEALTH_DOCTORS");
        poisTypes.put(32, "HEALTH_HOSPITALEMERGENCY");
        poisTypes.put(33, "HEALTH_HOSPITAL");
        poisTypes.put(34, "HEALTH_PHARMACY");
        poisTypes.put(35, "HEALTH_VETERINARY");
        poisTypes.put(36, "LANDUSE_ALLOTMENTS");
        poisTypes.put(37, "LANDUSE_CONIFEROUSDECIDUOUS");
        poisTypes.put(38, "LANDUSE_CONIFEROUS");
        poisTypes.put(39, "LANDUSE_DECIDUOUS");
        poisTypes.put(40, "LANDUSE_GRASS");
        poisTypes.put(41, "LANDUSE_HILLS");
        poisTypes.put(42, "LANDUSE_MILITARY");
        poisTypes.put(43, "LANDUSE_QUARY");
        poisTypes.put(44, "LANDUSE_SCRUB");
        poisTypes.put(45, "LANDUSE_SWAMP");
        poisTypes.put(46, "MONEY_BANK");
        poisTypes.put(47, "MONEY_EXCHANGE");
        poisTypes.put(48, "POW_BAHAI");
        poisTypes.put(49, "POW_BUDDHIST");
        poisTypes.put(50, "POW_CHRISTIAN");
		poisTypes.put(51, "POW_HINDU");
		poisTypes.put(52, "POW_ISLAMIC");
		poisTypes.put(53, "POW_JAIN");
		poisTypes.put(54, "POW_JEWISH");
		poisTypes.put(55, "POW_SHINTO");
		poisTypes.put(56, "POW_SIKH");
		poisTypes.put(58, "POW_UNKOWN");
		poisTypes.put(59, "POI_CAVE");
		poisTypes.put(60, "POI_CRANE");
		poisTypes.put(61, "POI_EMBASSY");
		poisTypes.put(62, "POI_BUNKER");
		poisTypes.put(63, "POI_MINE");
		poisTypes.put(64, "POI_PEAK1");
		poisTypes.put(65, "POI_PEAK");
		poisTypes.put(66, "POI_CITY");
		poisTypes.put(67, "POI_HAMLET");
		poisTypes.put(68, "POI_SUBURB");
		poisTypes.put(69, "POI_TOWN");
		poisTypes.put(70, "POI_VILLAGE");
		poisTypes.put(71, "POI_TOWERCOMMUNICATION");
		poisTypes.put(72, "POI_TOWERLOOKOUT");
		poisTypes.put(73, "SHOP_ALCOHOL");
		poisTypes.put(74, "SHOP_BAKERY");
		poisTypes.put(75, "SHOP_BICYCLE");
		poisTypes.put(76, "SHOP_BOOK");
		poisTypes.put(77, "SHOP_BUTCHER");
		poisTypes.put(78, "SHOP_CARREPAIR");
		poisTypes.put(79, "SHOP_CAR");
		poisTypes.put(80, "SHOP_CLOTHES");
		poisTypes.put(81, "SHOP_COMPUTER");
		poisTypes.put(82, "SHOP_CONFECTIONERY");
		poisTypes.put(83, "SHOP_CONVENIENCE");
		poisTypes.put(84, "SHOP_COPYSHOP");
		poisTypes.put(85, "SHOP_DEPARTMENTSTORE");
		poisTypes.put(86, "SHOP_DIY");
		poisTypes.put(87, "SHOP_FISH");
		poisTypes.put(88, "SHOP_FLORIST");
		poisTypes.put(89, "SHOP_GARDENCENTRE");
		poisTypes.put(90, "SHOP_GIFT");
		poisTypes.put(91, "SHOP_GREENGROCER");
		poisTypes.put(92, "SHOP_HAIRDRESSER");
		poisTypes.put(93, "SHOP_HEARINGAIDS");
		poisTypes.put(94, "SHOP_HIFI");
		poisTypes.put(95, "SHOP_JEWELRY");
		poisTypes.put(96, "SHOP_KIOSK");
		poisTypes.put(97, "SHOP_LAUNDRETTE");
		poisTypes.put(98, "SHOP_MARKETPLACE");
		poisTypes.put(99, "SHOP_PHONE");
		poisTypes.put(100, "SHOP_MOTORCYCLE");
		poisTypes.put(101, "SHOP_MUSIC");
		poisTypes.put(102, "SHOP_NEWSPAPER");
		poisTypes.put(103, "SHOP_PET");
		poisTypes.put(104, "SHOP_SHOES");
		poisTypes.put(105, "SHOP_SUPERMARKET");
		poisTypes.put(106, "SHOP_TOBACCO");
		poisTypes.put(107, "SHOP_TOYS");
		poisTypes.put(108, "SHOP_VENDINGMASCHINE");
		poisTypes.put(109, "SHOP_VIDEORENTAL");
		poisTypes.put(110, "SPORT_ARCHERY");
		poisTypes.put(111, "SPORT_BASEBALL");
		poisTypes.put(112, "SPORT_BASKETBALL");
		poisTypes.put(113, "SPORT_BOWLING");
		poisTypes.put(114, "SPORT_CANOE");
		poisTypes.put(115, "SPORT_CRICKET");
		poisTypes.put(116, "SPORT_DIVING");
		poisTypes.put(117, "SPORT_FOOTBALL");
		poisTypes.put(118, "SPORT_GOLF");
		poisTypes.put(119, "SPORT_GYM");
		poisTypes.put(120, "SPORT_GYMNASIUM");
		poisTypes.put(121, "SPORT_CLIMBING");
		poisTypes.put(122, "SPORT_HORSE");
		poisTypes.put(123, "SPORT_ICESKATING");
		poisTypes.put(124, "SPORT_LEISURECENTER");
		poisTypes.put(125, "SPORT_MINIATURGOLF");
		poisTypes.put(126, "SPORT_MOTORRACING");
		poisTypes.put(127, "SPORT_SHOOTING");
		poisTypes.put(128, "SPORT_SKATING");
		poisTypes.put(129, "SPORT_SKIINGDOWNHILL");
		poisTypes.put(130, "SPORT_SNOOKER");
		poisTypes.put(131, "SPORT_SOCCER");
		poisTypes.put(132, "SPORT_STADIUM");
		poisTypes.put(133, "SPORT_SWIMMING");
		poisTypes.put(134, "SPORT_TENNIS");
		poisTypes.put(135, "SPORT_WATERSKI");
		poisTypes.put(136, "SPORT_SURFING");
		poisTypes.put(137, "TOURIST_ARCHAELOGICAL");
		poisTypes.put(138, "TOURIST_ART");
		poisTypes.put(139, "TOURIST_ATTRACTION");
		poisTypes.put(140, "TOURIST_BATTLEFIELD");
		poisTypes.put(141, "TOURIST_BEACH");
		poisTypes.put(142, "TOURIST_CASTLE");
		poisTypes.put(143, "TOURIST_CASTLE2");
		poisTypes.put(144, "TOURIST_CINEMA");
		poisTypes.put(145, "TOURIST_FOUNTAIN");
		poisTypes.put(146, "TOURIST_INFORMATION");
		poisTypes.put(147, "TOURIST_MEMORIAL");
		poisTypes.put(148, "TOURIST_MONUMENT");
		poisTypes.put(149, "TOURIST_MUSEUM");
		poisTypes.put(150, "TOURIST_NIGHTCLUB");
		poisTypes.put(151, "TOURIST_RUINS");
		poisTypes.put(152, "TOURIST_THEATRE");
		poisTypes.put(153, "TOURIST_THEMEPARK");
		poisTypes.put(156, "TOURIST_WINDMILL");
		poisTypes.put(157, "TOURIST_WRECK");
		poisTypes.put(158, "TOURIST_ZOO");
		poisTypes.put(159, "TRANSPORT_TERMINAL");
		poisTypes.put(160, "TRANSPORT_AIRPORT");
		poisTypes.put(161, "TRANSPORT_BUSSTOP");
		poisTypes.put(162, "TRANSPORT_FUEL");
		poisTypes.put(163, "TRANSPORT_LIGHTHOUSE");
		poisTypes.put(164, "TRANSPORT_MARINA");
		poisTypes.put(165, "TRANSPORT_RENTALCAR");
		poisTypes.put(166, "TRANSPORT_SUBWAY");
		poisTypes.put(167, "TRANSPORT_STATION");
		poisTypes.put(168, "TRANSPORT_TRAMSTOP");
		poisTypes.put(169, "WATER_DAM");
		poisTypes.put(170, "WATER_TOWER");
		poisTypes.put(171, "WATER_WEIR");
    }

    public static final String pois = "[0: 'ALL', 1: 'ACCOMMO_ALPINEHUT', 2: 'ACCOMMO_CAMPING', 3: 'ACCOMMO_CARAVAN', 4: 'ACCOMMO_CHALET', 5: 'ACCOMMO_HOSTEL', 6: 'ACCOMMO_HOTEL',\n" +
			"7: 'ACCOMMO_MOTEL', 8: 'AMENITY_COURT', 9: 'AMENITY_FIRESTATION', 11:'AMENITY_LIBRARY', 12:'AMENITY_PLAYGROUND', 13: 'AMENITY_POLICE',\n" +
			"14:'AMENITY_POSTOFFICE', 15:'AMENITY_PRISON', 16: 'AMENITY_PUBLICBUILDING', 17:'AMENITY_TOWNHALL', 18:'BARRIER_BLOCKS', 19:'EDUCATION_COLLEGE',\n" +
			"20:'EDUCATION_NURSERY', 21:'EDUCATION_SCHOOL', 22:'EDUCATION_UNIVERSITY',23:'FOOD_BAR',24:'FOOD_BIERGARTEN',25:'FOOD_CAFE',\n" +
			"26:'FOOD_FASTFOOD',27: 'FOOD_ICECREAM', 28:'FOOD_PUB', 29:'FOOD_RESTAURANT',30:'HEALTH_DENTIST',31:'HEALTH_DOCTORS',32:'HEALTH_HOSPITALEMERGENCY',\n" +
			"33:'HEALTH_HOSPITAL',34:'HEALTH_PHARMACY',35:'HEALTH_VETERINARY',36:'LANDUSE_ALLOTMENTS',37:'LANDUSE_CONIFEROUSDECIDUOUS',38:'LANDUSE_CONIFEROUS',\n" +
			"39:'LANDUSE_DECIDUOUS', 40:'LANDUSE_GRASS',41:'LANDUSE_HILLS',42:'LANDUSE_MILITARY',43:'LANDUSE_QUARY',44:'LANDUSE_SCRUB',45:'LANDUSE_SWAMP',\n" +
			"46:'MONEY_BANK',47:'MONEY_EXCHANGE',48:'POW_BAHAI',49:'POW_BUDDHIST',50:'POW_CHRISTIAN',51:'POW_HINDU',52:'POW_ISLAMIC',53:'POW_JAIN',\n" +
			"54:'POW_JEWISH', 55:'POW_SHINTO', 56:'POW_SIKH',58:'POW_UNKOWN',59:'POI_CAVE',60:'POI_CRANE',61:'POI_EMBASSY',62:'POI_BUNKER',63:'POI_MINE',\n" +
			"64:'POI_PEAK1',65:'POI_PEAK',66:'POI_CITY',67:'POI_HAMLET',68:'POI_SUBURB',69:'POI_TOWN',70:'POI_VILLAGE',71:'POI_TOWERCOMMUNICATION',72:'POI_TOWERLOOKOUT',\n" +
			"73:'SHOP_ALCOHOL', 74:'SHOP_BAKERY',75:'SHOP_BICYCLE',76:'SHOP_BOOK',77:'SHOP_BUTCHER',78:'SHOP_CARREPAIR',79:'SHOP_CAR',80:'SHOP_CLOTHES',\n" +
			"81:'SHOP_COMPUTER',82:'SHOP_CONFECTIONERY',83:'SHOP_CONVENIENCE',84:'SHOP_COPYSHOP',85:'SHOP_DEPARTMENTSTORE',86:'SHOP_DIY',87:'SHOP_FISH',\n" +
			"88:'SHOP_FLORIST',89:'SHOP_GARDENCENTRE',90:'SHOP_GIFT',91:'SHOP_GREENGROCER',92:'SHOP_HAIRDRESSER',93:'SHOP_HEARINGAIDS',94:'SHOP_HIFI',\n" +
			"95:'SHOP_JEWELRY', 96:'SHOP_KIOSK',97:'SHOP_LAUNDRETTE',98:'SHOP_MARKETPLACE',99:'SHOP_PHONE',100:'SHOP_MOTORCYCLE',101:'SHOP_MUSIC',\n" +
			"102:'SHOP_NEWSPAPER',103:'SHOP_PET',104:'SHOP_SHOES',105:'SHOP_SUPERMARKET',106:'SHOP_TOBACCO',107:'SHOP_TOYS',108:'SHOP_VENDINGMASCHINE',\n" +
			"109:'SHOP_VIDEORENTAL',110:'SPORT_ARCHERY',111:'SPORT_BASEBALL', 112:'SPORT_BASKETBALL',113:'SPORT_BOWLING',114:'SPORT_CANOE',\n" +
			"115:'SPORT_CRICKET',116:'SPORT_DIVING',117:'SPORT_FOOTBALL',118:'SPORT_GOLF',119:'SPORT_GYM',120:'SPORT_GYMNASIUM',121:'SPORT_CLIMBING',\n" +
			"122:'SPORT_HORSE',123:'SPORT_ICESKATING',124:'SPORT_LEISURECENTER',125:'SPORT_MINIATURGOLF', 126:'SPORT_MOTORRACING',127:'SPORT_SHOOTING',\n" +
			"128:'SPORT_SKATING',129:'SPORT_SKIINGDOWNHILL',130:'SPORT_SNOOKER',131:'SPORT_SOCCER',132:'SPORT_STADIUM',133:'SPORT_SWIMMING',\n" +
			"134:'SPORT_TENNIS',135:'SPORT_WATERSKI',136:'SPORT_SURFING',137:'TOURIST_ARCHAELOGICAL',138:'TOURIST_ART',139:'TOURIST_ATTRACTION',\n" +
			"140:'TOURIST_BATTLEFIELD',141:'TOURIST_BEACH',142:'TOURIST_CASTLE',143:'TOURIST_CASTLE2',144:'TOURIST_CINEMA',145:'TOURIST_FOUNTAIN',\n" +
			"146:'TOURIST_INFORMATION',147:'TOURIST_MEMORIAL',148:'TOURIST_MONUMENT',149:'TOURIST_MUSEUM',150:'TOURIST_NIGHTCLUB', 151:'TOURIST_RUINS',\n" +
			"152:'TOURIST_THEATRE',153:'TOURIST_THEMEPARK',156:'TOURIST_WINDMILL',157:'TOURIST_WRECK',158:'TOURIST_ZOO',159:'TRANSPORT_TERMINAL',\n" +
			"160:'TRANSPORT_AIRPORT',161:'TRANSPORT_BUSSTOP',162:'TRANSPORT_FUEL',163:'TRANSPORT_LIGHTHOUSE',164:'TRANSPORT_MARINA',165:'TRANSPORT_RENTALCAR',\n" +
			"166:'TRANSPORT_SUBWAY',167:'TRANSPORT_STATION',168:'TRANSPORT_TRAMSTOP',169:'WATER_DAM',170:'WATER_TOWER',171:'WATER_WEIR']";
}

