# PoiReader.py #

from __future__ import print_function
from pyspark import SparkContext
from pyspark.sql import Row
from pymongo import MongoClient
import os.path
import time

#POIs

def parsePoi(poi):

        fields = poi.split('|')
        if (len(fields)<5):
            return (poi, 0)
        else:
            #print (fields)
            try:
                row = Row(
                    type      = transformType(checkInteger(fields[0])),   #type
                    osm_id    = fields[1],   #osm-id
                    latitude     = checkFloat(fields[2]), #latitude
                    longitude = checkFloat(fields[3]),   #longitude
                    name = fields[4]        # name
                )
    
                return (row,1)

            except:
                return (poi, 0)
            


def transformType(intType):
    if (intType is not None):
        return poisTypes[intType]
    else:
        return None


def checkInteger(field):
    if not field:
        return None
    else:
        return int(field)

def checkFloat(field):
    if not field:
        return None
    else:
        return float(field)

def insertPoisMongoDb(poiList):

     for poiRow in poiList:
        if poiRow is not None:
            poi = {
                "poi_type": poiRow.type,
                "poi_osm_id": poiRow.osm_id,
                "poi_coord": [poiRow.longitude, poiRow.latitude],
                "poi_name": poiRow.name,
            }

            #print("Saving poi in database: %s" % poi)

            try:
                db.pois.insert(poi)
            except:
                pass

def parsePoiAndSave(poisCsv, name):
    poisCsv = sc.textFile(poisCsv)
    pois = (poisCsv.map(parsePoi).cache())

    poisOk = (pois.filter(lambda s: s[1] == 1)
                      .map(lambda s: s[0])
                      .cache())

    poisError = (pois.filter(lambda s: s[1] == 0)
                         .map(lambda s: s[0]))

    insertPoisMongoDb(poisOk.toLocalIterator())

    print ('\nRead %d pois in %s, %d succesfully parsed, %d with wrong format \n' %(pois.count(), name, poisOk.count(), poisError.count()))

    if poisError.count() > 0:
        print('Pois %s with wrong format: %d \n'% (name, poisError.count()))
        for line in poisError.collect():
            line = unicode(line).encode('utf8')
            print(line)
        print('\n')


if __name__ == "__main__":

    sc = SparkContext(appName="HotelReader", master="local[4]")

    poisTypes = {1: 'ACCOMMO_ALPINEHUT', 2: 'ACCOMMO_CAMPING', 3: 'ACCOMMO_CARAVAN', 4: 'ACCOMMO_CHALET', 5: 'ACCOMMO_HOSTEL', 6: 'ACCOMMO_HOTEL',
            7: 'ACCOMMO_MOTEL', 8: 'AMENITY_COURT', 9: 'AMENITY_FIRESTATION', 11:'AMENITY_LIBRARY', 12:'AMENITY_PLAYGROUND', 13: 'AMENITY_POLICE',
            14:'AMENITY_POSTOFFICE', 15:'AMENITY_PRISON', 16: 'AMENITY_PUBLICBUILDING', 17:'AMENITY_TOWNHALL', 18:'BARRIER_BLOCKS', 19:'EDUCATION_COLLEGE',
            20:'EDUCATION_NURSERY', 21:'EDUCATION_SCHOOL', 22:'EDUCATION_UNIVERSITY',23:'FOOD_BAR',24:'FOOD_BIERGARTEN',25:'FOOD_CAFE',
            26:'FOOD_FASTFOOD',27: 'FOOD_ICECREAM', 28:'FOOD_PUB', 29:'FOOD_RESTAURANT',30:'HEALTH_DENTIST',31:'HEALTH_DOCTORS',32:'HEALTH_HOSPITALEMERGENCY',
            33:'HEALTH_HOSPITAL',34:'HEALTH_PHARMACY',35:'HEALTH_VETERINARY',36:'LANDUSE_ALLOTMENTS',37:'LANDUSE_CONIFEROUSDECIDUOUS',38:'LANDUSE_CONIFEROUS',
            39:'LANDUSE_DECIDUOUS', 40:'LANDUSE_GRASS',41:'LANDUSE_HILLS',42:'LANDUSE_MILITARY',43:'LANDUSE_QUARY',44:'LANDUSE_SCRUB',45:'LANDUSE_SWAMP',
            46:'MONEY_BANK',47:'MONEY_EXCHANGE',48:'POW_BAHAI',49:'POW_BUDDHIST',50:'POW_CHRISTIAN',51:'POW_HINDU',52:'POW_ISLAMIC',53:'POW_JAIN',
            54:'POW_JEWISH', 55:'POW_SHINTO', 56:'POW_SIKH',58:'POW_UNKOWN',59:'POI_CAVE',60:'POI_CRANE',61:'POI_EMBASSY',62:'POI_BUNKER',63:'POI_MINE',
            64:'POI_PEAK1',65:'POI_PEAK',66:'POI_CITY',67:'POI_HAMLET',68:'POI_SUBURB',69:'POI_TOWN',70:'POI_VILLAGE',71:'POI_TOWERCOMMUNICATION',72:'POI_TOWERLOOKOUT',
            73:'SHOP_ALCOHOL', 74:'SHOP_BAKERY',75:'SHOP_BICYCLE',76:'SHOP_BOOK',77:'SHOP_BUTCHER',78:'SHOP_CARREPAIR',79:'SHOP_CAR',80:'SHOP_CLOTHES',
            81:'SHOP_COMPUTER',82:'SHOP_CONFECTIONERY',83:'SHOP_CONVENIENCE',84:'SHOP_COPYSHOP',85:'SHOP_DEPARTMENTSTORE',86:'SHOP_DIY',87:'SHOP_FISH',
            88:'SHOP_FLORIST',89:'SHOP_GARDENCENTRE',90:'SHOP_GIFT',91:'SHOP_GREENGROCER',92:'SHOP_HAIRDRESSER',93:'SHOP_HEARINGAIDS',94:'SHOP_HIFI',
            95:'SHOP_JEWELRY', 96:'SHOP_KIOSK',97:'SHOP_LAUNDRETTE',98:'SHOP_MARKETPLACE',99:'SHOP_PHONE',100:'SHOP_MOTORCYCLE',101:'SHOP_MUSIC',
            102:'SHOP_NEWSPAPER',103:'SHOP_PET',104:'SHOP_SHOES',105:'SHOP_SUPERMARKET',106:'SHOP_TOBACCO',107:'SHOP_TOYS',108:'SHOP_VENDINGMASCHINE',
            109:'SHOP_VIDEORENTAL',110:'SPORT_ARCHERY',111:'SPORT_BASEBALL', 112:'SPORT_BASKETBALL',113:'SPORT_BOWLING',114:'SPORT_CANOE',
            115:'SPORT_CRICKET',116:'SPORT_DIVING',117:'SPORT_FOOTBALL',118:'SPORT_GOLF',119:'SPORT_GYM',120:'SPORT_GYMNASIUM',121:'SPORT_CLIMBING',
            122:'SPORT_HORSE',123:'SPORT_ICESKATING',124:'SPORT_LEISURECENTER',125:'SPORT_MINIATURGOLF',126:'SPORT_MOTORRACING',127:'SPORT_SHOOTING',
            128:'SPORT_SKATING',129:'SPORT_SKIINGDOWNHILL',130:'SPORT_SNOOKER',131:'SPORT_SOCCER',132:'SPORT_STADIUM',133:'SPORT_SWIMMING',
            134:'SPORT_TENNIS',135:'SPORT_WATERSKI',136:'SPORT_SURFING',137:'TOURIST_ARCHAELOGICAL',138:'TOURIST_ART',139:'TOURIST_ATTRACTION',
            140:'TOURIST_BATTLEFIELD',141:'TOURIST_BEACH',142:'TOURIST_CASTLE',143:'TOURIST_CASTLE2',144:'TOURIST_CINEMA',145:'TOURIST_FOUNTAIN',
            146:'TOURIST_INFORMATION',147:'TOURIST_MEMORIAL',148:'TOURIST_MONUMENT',149:'TOURIST_MUSEUM',150:'TOURIST_NIGHTCLUB', 151:'TOURIST_RUINS',
            152:'TOURIST_THEATRE',153:'TOURIST_THEMEPARK',156:'TOURIST_WINDMILL',157:'TOURIST_WRECK',158:'TOURIST_ZOO',159:'TRANSPORT_TERMINAL',
            160:'TRANSPORT_AIRPORT',161:'TRANSPORT_BUSSTOP',162:'TRANSPORT_FUEL',163:'TRANSPORT_LIGHTHOUSE',164:'TRANSPORT_MARINA',165:'TRANSPORT_RENTALCAR',
            166:'TRANSPORT_SUBWAY',167:'TRANSPORT_STATION',168:'TRANSPORT_TRAMSTOP',169:'WATER_DAM',170:'WATER_TOWER',171:'WATER_WEIR'}

    #POIS

    client = MongoClient()

    db = client['HotelAdvisor']

    startProcess = time.time()

    poisCentralAmericaCsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'central-america-latest.csv')
    parsePoiAndSave(poisCentralAmericaCsv, 'Central America')

    poisAfricaCsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'africa-latest.csv')
    parsePoiAndSave(poisAfricaCsv, 'Africa')

    poisAsiaCsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'asia-latest.csv')
    parsePoiAndSave(poisAsiaCsv, 'Asia')

    poisEuropeCsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'europe-latest.csv')
    parsePoiAndSave(poisEuropeCsv, 'Europe')

    poisNorthAmericaCsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'north-america-latest.csv')
    parsePoiAndSave(poisNorthAmericaCsv, 'North America')

    poisOceaniaCsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'australia-oceania-latest.csv')
    parsePoiAndSave(poisOceaniaCsv, 'Oceania')

    poisSouthAmericaCsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'south-america-latest.csv')
    parsePoiAndSave(poisSouthAmericaCsv, 'South America')


    endProcess = time.time()

    print(endProcess - startProcess)

