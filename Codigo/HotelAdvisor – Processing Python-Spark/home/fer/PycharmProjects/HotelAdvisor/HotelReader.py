# HotelReader.py #

from __future__ import print_function
from pyspark import SparkContext
from pyspark.sql import Row
from pymongo import MongoClient
import os.path
import time

#HOTELS

def getCountry(cc1):
    if not cc1:
        return None
    else:
        return countries[cc1.upper()]

def getContinent(continent_id):
    if not continent_id:
        return None
    else:
        return continents[continent_id]

def parseHotel(hotel):
    fields = hotel.split("\t")
    #print (fields)
    if (len(fields) < 38):
        return (hotel, 0)
    else:

        try:
            row = Row(
                id      = int(fields[0]),   #id
                name    = fields[1],        #name
                address = fields[2],        #address
                zip     = fields[3],        #zip (there are zips with letters like 'H100')
                city_hotel = fields[4],     #city_hotel
                cc1 =       getCountry(fields[5]),  # cc1
                ufi     = checkInteger(fields[6]),   #ufi
                clazz   = checkFloat(fields[7]), #ufi
                currencycode= fields[8],    #currencycode
                minrate= checkFloat(fields[9]),    #minrate
                maxrate= checkFloat(fields[10]),   #maxrate
                preferred= checkInteger(fields[11]), #preferred
                nr_rooms= checkInteger(fields[12]),  #nr_rooms
                longitude=checkFloat(fields[13]), #longitude
                latitude=checkFloat(fields[14]), #latitude
                public_ranking= checkInteger(fields[15]), #public_ranking
                hotel_url= fields[16],    #hotel_url
                photo_url= fields[17],    #photo_url
                desc_en= fields[18],     #desc_en
                #desc_fr= fields[19],     #desc_fr
                desc_es= fields[20],     #desc_es
                #desc_de= fields[21],     #desc_de
                #desc_nl= fields[22],     #desc_nl
                #desc_it= fields[23],     #desc_it
                #desc_pt= fields[24],     #desc_pt
                #desc_ja= fields[25],     #desc_ja
                #desc_zh= fields[26],     #desc_zh
                #desc_pl= fields[27],     #desc_pl
                #desc_ru= fields[28],     #desc_ru
                #desc_sv= fields[29],     #desc_sv
                #desc_ar= fields[30],     #desc_ar
                #desc_el= fields[31],     #desc_el
                #desc_no= fields[32],     #desc_no
                city_unique= fields[33],     #city_preferred
                city_preferred= fields[34],    #city_preferred
                continent_id=getContinent(checkInteger(fields[35])),  # continent_id
                review_score= float(0),  #review_score
                review_nr= 0 #review_nr
                #minrateEUR = calculateRateEUR(fields[8], checkFloat(fields[9])) , # minrateEUR
                #maxrateEUR = calculateRateEUR(fields[8], checkFloat(fields[10]))  # maxrateEUR
            )

            return (row,1)
        except:
            return (hotel, 0)

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

def calculateRateEUR(currencycode, rate):
	if not rate:
		return None
	else:
		if (currencycode=='EUR'):
			return rate
		else:
			exchange = money[currencycode]
			if not exchange:
				return None
			else:
				return rate/exchange

def insertHotelsMongoDb(hotelList):

    global numHotels;

    for hotelRow in hotelList:

        hotel = {
            "id": hotelRow[1] + numHotels,
            "name": hotelRow[0].name,
            "address": hotelRow[0].address,
            "zip": hotelRow[0].zip,
            "city_hotel": hotelRow[0].city_hotel,
            "cc1": hotelRow[0].cc1,
            "ufi": hotelRow[0].ufi,
            "class": hotelRow[0].clazz,
            "currencycode": hotelRow[0].currencycode,
            "minrate": hotelRow[0].minrate,
            "maxrate": hotelRow[0].maxrate,
            "preferred": hotelRow[0].preferred,
            "nr_rooms": hotelRow[0].nr_rooms,
            "coord": [hotelRow[0].longitude, hotelRow[0].latitude],
            "public_ranking": hotelRow[0].public_ranking,
            "hotel_url": hotelRow[0].hotel_url,
            "photo_url": hotelRow[0].photo_url,
            "desc_en": hotelRow[0].desc_en,
            "desc_es": hotelRow[0].desc_es,
            "city_unique": hotelRow[0].city_unique,
            "city_preferred": hotelRow[0].city_preferred,
            "continent_id": hotelRow[0].continent_id,
            "review_score": hotelRow[0].review_score,
            "review_nr": hotelRow[0].review_nr,
            "min_rate_EUR": calculateRateEUR(hotelRow[0].currencycode, hotelRow[0].minrate),
            "max_rate_EUR": calculateRateEUR(hotelRow[0].currencycode, hotelRow[0].maxrate)
        }

        #print("Saving hotel in database: %s" % hotel)

        db.hotels.insert(hotel)

def parseHotelAndSave(hotelsTsv, name):
    hotelsTsv = sc.textFile(hotelsTsv)
    header = hotelsTsv.first()  # Extract header
    hotelsTsv = hotelsTsv.filter(lambda row: row != header)
    hotels= (hotelsTsv.map(parseHotel).cache())

    hotelsOk = (hotels.filter(lambda s: s[1] == 1)
                      .map(lambda s: s[0])
                      .cache())

    hotelsError = (hotels.filter(lambda s: s[1] == 0)
                         .map(lambda s: s[0]))

    insertHotelsMongoDb(hotelsOk.zipWithIndex().collect())

    print ('\nRead %d hotels in %s, %d succesfully parsed, %d with wrong format \n' %(hotels.count(), name, hotelsOk.count(), hotelsError.count()))

    if hotelsError.count() > 0:
        print('Hotels %s with wrong format: %d \n'% (name, hotelsError.count()))
        for line in hotelsError.collect():
            print (line)
        print('\n')

    return hotelsOk.count()



if __name__ == "__main__":

    countries = {
        'AD': 'Andorra',
        'AE': 'United Arab Emirates',
        'AF': 'Afghanistan',
        'AG': 'Antigua and Barbuda',
        'AI': 'Anguilla',
        'AL': 'Albania',
        'AM': 'Armenia',
        'AO': 'Angola',
        'AQ': 'Antarctica',
        'AR': 'Argentina',
        'AS': 'American Samoa',
        'AT': 'Austria',
        'AU': 'Australia',
        'AW': 'Aruba',
        'AX': 'Aland Islands',
        'AZ': 'Azerbaijan',
        'BA': 'Bosnia',
        'BB': 'Barbados',
        'BD': 'Bangladesh',
        'BE': 'Belgium',
        'BF': 'Burkina Faso',
        'BG': 'Bulgaria',
        'BH': 'Bahrain',
        'BI': 'Burundi',
        'BJ': 'Benin',
        'BL': 'Saint Barthelemy',
        'BM': 'Bermuda',
        'BN': 'Brunei',
        'BO': 'Bolivia',
        'BQ': 'Bonaire, Sint Eustatius and Saba',
        'BR': 'Brazil',
        'BS': 'Bahamas',
        'BT': 'Bhutan',
        'BV': 'Bouvet Island',
        'BW': 'Botswana',
        'BY': 'Belarus',
        'BZ': 'Belize',
        'CA': 'Canada',
        'CC': 'Cocos Islands',
        'CD': 'Democratic Republic of Congo',
        'CF': 'Central African Republic',
        'CG': 'Congo',
        'CH': 'Switzerland',
        'CI': 'Ivory Coast',
        'CK': 'Cook Islands',
        'CL': 'Chile',
        'CM': 'Cameroon',
        'CN': 'China',
        'CO': 'Colombia',
        'CR': 'Costa Rica',
        'CU': 'Cuba',
        'CV': 'Cabo Verde',
        'CW': 'Curacao',
        'CX': 'Christmas Island',
        'CY': 'Cyprus',
        'CZ': 'Czechia',
        'DE': 'Germany',
        'DJ': 'Djibouti',
        'DK': 'Denmark',
        'DM': 'Dominica',
        'DO': 'Dominican Republic',
        'DZ': 'Algeria',
        'EC': 'Ecuador',
        'EE': 'Estonia',
        'EG': 'Egypt',
        'EH': 'Western Sahara',
        'ER': 'Eritrea',
        'ES': 'Spain',
        'ET': 'Ethiopia',
        'FI': 'Finland',
        'FJ': 'Fiji',
        'FK': 'Falkland Islands',
        'FM': 'Micronesia',
        'FO': 'Faroe Islands',
        'FR': 'France',
        'GA': 'Gabon',
        'GB': 'United Kingdom',
        'GD': 'Grenada',
        'GE': 'Georgia',
        'GF': 'French Guiana',
        'GG': 'Guernsey',
        'GH': 'Ghana',
        'GI': 'Gibraltar',
        'GL': 'Greenland',
        'GM': 'Gambia',
        'GN': 'Guinea',
        'GP': 'Guadeloupe',
        'GQ': 'Equatorial Guinea',
        'GR': 'Greece',
        'GS': 'South Georgia and the South Sandwich Islands',
        'GT': 'Guatemala',
        'GU': 'Guam',
        'GW': 'Guinea-Bissau',
        'GY': 'Guyana',
        'HK': 'Hong Kong',
        'HM': 'Heard Island and McDonald Islands',
        'HN': 'Honduras',
        'HR': 'Croatia',
        'HT': 'Haiti',
        'HU': 'Hungary',
        'ID': 'Indonesia',
        'IE': 'Ireland',
        'IL': 'Israel',
        'IM': 'Isle of Man',
        'IN': 'India',
        'IO': 'British Indian Ocean Territory',
        'IQ': 'Iraq',
        'IR': 'Iran',
        'IS': 'Iceland',
        'IT': 'Italy',
        'JE': 'Jersey',
        'JM': 'Jamaica',
        'JO': 'Jordan',
        'JP': 'Japan',
        'KE': 'Kenya',
        'KG': 'Kyrgyzstan',
        'KH': 'Cambodia',
        'KI': 'Kiribati',
        'KM': 'Comoros',
        'KN': 'Saint Kitts and Nevis',
        'KP': 'North Korea',
        'KR': 'South Korea',
        'KW': 'Kuwait',
        'KY': 'Cayman Islands',
        'KZ': 'Kazakhstan',
        'LA': 'Laos',
        'LB': 'Lebanon',
        'LC': 'Saint Lucia',
        'LI': 'Liechtenstein',
        'LK': 'Sri Lanka',
        'LR': 'Liberia',
        'LS': 'Lesotho',
        'LT': 'Lithuania',
        'LU': 'Luxembourg',
        'LV': 'Latvia',
        'LY': 'Libya',
        'MA': 'Morocco',
        'MC': 'Monaco',
        'MD': 'Moldova',
        'ME': 'Montenegro',
        'MF': 'Saint Martin',
        'MG': 'Madagascar',
        'MH': 'Marshall Islands',
        'MK': 'Macedonia',
        'ML': 'Mali',
        'MM': 'Myanmar',
        'MN': 'Mongolia',
        'MO': 'Macao',
        'MP': 'Northern Mariana Islands',
        'MQ': 'Martinique',
        'MR': 'Mauritania',
        'MS': 'Montserrat',
        'MT': 'Malta',
        'MU': 'Mauritius',
        'MV': 'Maldives',
        'MW': 'Malawi',
        'MX': 'Mexico',
        'MY': 'Malaysia',
        'MZ': 'Mozambique',
        'NA': 'Namibia',
        'NC': 'New Caledonia',
        'NE': 'Niger',
        'NF': 'Norfolk Island',
        'NG': 'Nigeria',
        'NI': 'Nicaragua',
        'NL': 'Netherlands',
        'NO': 'Norway',
        'NP': 'Nepal',
        'NR': 'Nauru',
        'NU': 'Niue',
        'NZ': 'New Zealand',
        'OM': 'Oman',
        'PA': 'Panama',
        'PE': 'Peru',
        'PF': 'French Polynesia',
        'PG': 'Papua New Guinea',
        'PH': 'Philippines',
        'PK': 'Pakistan',
        'PL': 'Poland',
        'PM': 'Saint Pierre and Miquelon',
        'PN': 'Pitcairn',
        'PR': 'Puerto Rico',
        'PS': 'Palestine',
        'PT': 'Portugal',
        'PW': 'Palau',
        'PY': 'Paraguay',
        'QA': 'Qatar',
        'RE': 'Reunion',
        'RO': 'Romania',
        'RS': 'Serbia',
        'RU': 'Russia',
        'RW': 'Rwanda',
        'SA': 'Saudi Arabia',
        'SB': 'Solomon Islands',
        'SC': 'Seychelles',
        'SD': 'Sudan',
        'SE': 'Sweden',
        'SG': 'Singapore',
        'SH': 'Saint Helena, Ascension and Tristan da Cunha',
        'SI': 'Slovenia',
        'SJ': 'Svalbard and Jan Mayen',
        'SK': 'Slovakia',
        'SL': 'Sierra Leone',
        'SM': 'San Marino',
        'SN': 'Senegal',
        'SO': 'Somalia',
        'SR': 'Suriname',
        'SS': 'South Sudan',
        'ST': 'Sao Tome and Principe',
        'SV': 'El Salvador',
        'SX': 'Sint Maarten',
        'SY': 'Syrian Arab Republic',
        'SZ': 'Swaziland',
        'TC': 'Turks and Caicos Islands',
        'TD': 'Chad',
        'TF': 'French Southern Territories',
        'TG': 'Togo',
        'TH': 'Thailand',
        'TJ': 'Tajikistan',
        'TK': 'Tokelau',
        'TL': 'Timor-Leste',
        'TM': 'Turkmenistan',
        'TN': 'Tunisia',
        'TO': 'Tonga',
        'TR': 'Turkey',
        'TT': 'Trinidad and Tobago',
        'TV': 'Tuvalu',
        'TW': 'Taiwan',
        'TZ': 'Tanzania',
        'UA': 'Ukraine',
        'UG': 'Uganda',
        'UM': 'United States Minor Outlying Islands',
        'US': 'United States of America',
        'UY': 'Uruguay',
        'UZ': 'Uzbekistan',
        'VA': 'Holy See',
        'VC': 'Saint Vincent and the Grenadines',
        'VE': 'Venezuela',
        'VG': 'Virgin Islands, British',
        'VI': 'Virgin Islands, U.S.',
        'VN': 'Vietnam',
        'VU': 'Vanuatu',
        'WF': 'Wallis and Futuna',
        'WS': 'Samoa',
        'XA': 'Abjasia',
        'XC': 'Crimea',
        'XK': 'Kosovo',
        'YE': 'Yemen',
        'YT': 'Mayotte',
        'ZA': 'South Africa',
        'ZM': 'Zambia',
        'ZW': 'Zimbabwe'
    }

    continents = {
        1: 'North America',
        2: 'Central America',
        3: 'South America',
        5: 'Africa',
        6: 'Europe',
        7: 'Asia',
        8: 'Asia',
        9: 'Oceania',
        10: 'Central America'
    }

    money = {

        # AFRICA
        'ETB': 24.16548,  # Birr Etiopiano
        'GHS': 4.68786,  # Cedi de Ghana
        'KES': 110.73610,  # Chelin Keniano
        'SOS': 620.16460,  # Chelin Somali
        'TZS': 2386.64600,  # Chelin Tanzano
        'UGX': 3826.10700,  # Chelin Ugandes
        'GMD': 47.39126,  # Dalasi Gambiano
        'DZD': 117.15290,  # Dinar Argelino
        'LYD': 1.49499,  # Dinar Libio
        'TND': 2.44153,  # Dinar Tunisio
        'MAD': 10.71738,  # Dirham Marroqui
        'NAD': 14.35724,  # Dolar Nambies
        'CVE': 109.40120,  # Escudo de Cabo Verde
        'XAF': 656.35660,  # Franco Africano
        'BIF': 1809.79400,  # Franco Burundi
        'XOF': 656.35660,  # Franco CFA de Africa Occidental
        'DJF': 191.11310,  # Franco Dijibouti
        'GNF': 9989.75200,  # Franco Guineano
        'RWF': 876.17240,  # Franco Ruandes
        'MWK': 773.41280,  # Kwacha de Malaui
        'ZMW': 10.51832,  # Kwacha Zambiano
        'AOA': 177.00260,  # Kwanza Angoleno
        'EGP': 19.56620,  # Libra Egipcia
        'SZL': 14.35724,  # Lilangeni de Suazilandia
        'LSL': 14.35724,  # Loti de Lesoto
        'NGN': 336.05250,  # Naira Nigeriana
        'BWP': 11.23806,  # Pula De Botsuana
        'ZAR': 14.36380,  # Rand Sudafricano
        'MUR': 37.96209,  # Rupia Mauricia
        'MZN': 75.8417,  # Metical de Mozambique

        # ASIA
        'BHD': 0.40258,  # Dinar Bahrini
        'IQD': 1267.16400,  # Dinar Iraqui
        'JOD': 0.75711,  # Dinar Jordano
        'KWD': 0.32590,  # Dinar Kuwaiti
        'AED': 3.92211,  # Dirham de Emiratos Arabes Unidos
        'AMD': 518.75090,  # Dram Armeniano
        'GEL': 2.84048,  # Lari Georgiano
        'LBP': 1611.38600,  # Libra Libanesa
        'TRY': 3.99291,  # Lira Turca
        'AZN': 2.00249,  # Manat Azerbaiyano
        'TMT': 3.73748,  # Manat Turcomano
        'IRR': 34567.37000,  # EUR	 Rial Irani
        'OMR': 0.41128,  # Rial Omani
        'QAR': 3.88880,  # Rial Qatari
        'SAR': 4.00540,  # Rial Saudi
        'YER': 267.12270,  # Rial Yemeni
        'ILS': 4.00740,  # Shequel Israeli
        'UZS': 3518.28800,  # Som Uzbeko
        'KZT': 345.58830,  # Tenge Kazako
        'THB': 37.42100,  # Baht Tailandes
        'BND': 1.51480,  # Dolar de Brunei
        'HKD': 8.28430,  # Dolar de Hong Kong
        'SGD': 1.51440,  # Dolar Singapurense
        'TWD': 33.13806,  # Dolar Taiwanes
        'VND': 24144.09000,  # Dolar Vietnamis
        'LAK': 8786.75000,  # Kip de Laos
        'MMK': 1459.96500,  # Kyat de Myanmar
        'MOP': 8.53378,  # Pataca de Macao
        'PHP': 53.14100,  # Peso Filipino
        'KHR': 4332,  # Riel Camboyano
        'MYR': 4.74360,  # Ringgit Malayo
        'SCR': 14.46941,  # Rupia de Seychelles
        'LKR': 160.92500,  # Rupia de Sri Lanka
        'INR': 71.93900,  # Rupia Hindu
        'IDR': 14233.37000,  # Rupia Indonesa
        'NPR': 115.11420,  # Rupia Nepalesa
        'PKR': 111.91070,  # Rupia Paquistani
        'BDT': 85.26784,  # Taka Banglades
        'KRW': 1223.97000,  # Won Coreano
        'JPY': 119.95350,  # Yen Japones
        'CNY': 7.35210,  # Yuan Chino
        'PGK': 3.39560, #Kina de Papua Nueva Guinea
        'AFN': 71.4317,#Afani Afgano

        # EUROPA
        'CZK': 27.02150,  # Corona Checa
        'DKK': 7.43455,  # Corona Danesa
        'ISK': 121.57470,  # Corona Islandesa
        'NOK': 8.87630,  # Corona Noruega
        'SEK': 9.47151,  # Corona Sueca
        'RSD': 123.89000,  # Dinar Serbio
        'HUF': 309.34000,  # Forint de Hungria
        'CHF': 1.06616,  # Franco Suizo
        'UAH': 28.83729,  # Hryvnia Ucraniano
        'HRK': 7.44790,  # Kuna Croata
        'ALL': 136.10000,  # Lek Albanes
        'BGN': 1.95690,  # Leu de Bulgaria
        'MDL': 21.17547,  # Leu Moldavo
        'RON': 4.49000,  # Leu Rumano
        'GBP': 0.86247,  # Libra Esterlina
        'BYN': 2.04974,  # Rublo Bielorruso
        'RUB': 63.27790,  # Rublo Ruso
        'PLN': 4.30430,  # Zloty Polaco

        # OCEANIA
        'AUD': 1.40076,  # Dolar Australiano
        'FJD': 2.18557,  # Dolar de Fiji
        'NZD': 1.46410,  # Dolar Neozelandes
        'XPF': 119.40440,  # Franco de las Islas Pacifico Francesas
        'VUV': 118.293, #Vatu de Vanuatu

        # AMERICA
        'PAB': 1.06785,  # Balboa Panameno
        'VEF': 10.66254,  # Bolivar Venezolano
        'CRC': 594.71850,  # Colon Costarricense
        'NIO': 31.47381,  # Cordoba Nicaraguense
        'CAD': 1.40486,  # Dolar Canadiense
        'BBD': 2.13570,  # Dolar de Barbados
        'BZD': 2.14638,  # Dolar de Belice
        'BMD': 1.06785,  # Dolar de Bermudas
        'BSD': 1.06785,  # Dolar de las Bahamas
        'KYD': 0.87564,  # Dolar de las Islas Caiman
        'TTD': 7.21349,  # Dolar de Trinidad y Tobago
        'XCD': 2.88320,  # Dolar del Caribe Oriental
        'USD': 1.06777,  # Dolar Estadounidense
        'JMD': 137.69930,  # Dolar Jamaicano
        'PYG': 6125.01200,  # Guarani Paraguayo
        'ANG': 1.91145,  # Guilder Antillano
        'HTG': 72.52720,  # Gurda Haitiana
        'HNL': 25.19058,  # Lempira Hondurena
        'PEN': 3.51002,  # Nuevo Sol Peruano
        'ARS': 16.72900,  # Peso Argentino
        'BOB': 7.40021,  # Peso Boliviano
        'CLP': 691.71000,  # Peso Chileno
        'COP': 3066.06400,  # Peso Colombiano
        'CUP': 1.06785,  # Peso Cubano
        'DOP': 49.82057,  # Peso Dominicano
        'MXN': 22.05650,  # Peso Mexicano
        'UYU': 30.33900,  # Peso Uruguayo
        'GTQ': 7.94534,  # Quetzal Guatemalteco
        'BRL': 3.34310  # Real Brasileno
    }


    sc = SparkContext(appName="HotelReader", master="local")

    #HOTELS

    client = MongoClient()

    db = client['HotelAdvisor']

    startProcess = time.time()

    numHotels= 1

    hotelsAfricaTsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'Hotels_Africa_1.tsv')
    numHotelsOk = parseHotelAndSave(hotelsAfricaTsv, 'Africa')
    numHotels = numHotels + numHotelsOk

    hotelsAsiaTsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'Hotels_Asia_1.tsv')
    numHotelsOk = parseHotelAndSave(hotelsAsiaTsv, 'Asia')
    numHotels = numHotels + numHotelsOk


    hotelsAsia2Tsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'Hotels_Asia_2.tsv')
    numHotelsOk = parseHotelAndSave(hotelsAsia2Tsv, 'Asia2')
    numHotels = numHotels + numHotelsOk

    hotelsAsia3Tsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'Hotels_Asia_3.tsv')
    numHotelsOk = parseHotelAndSave(hotelsAsia3Tsv, 'Asia3')
    numHotels = numHotels + numHotelsOk

    hotelsOceaniaTsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'Hotels_AustraliaandOceania_1.tsv')
    numHotelsOk = parseHotelAndSave(hotelsOceaniaTsv, 'Oceania')
    numHotels = numHotels + numHotelsOk

    hotelsCaribbeanTsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'Hotels_Caribbean_1.tsv')
    numHotelsOk = parseHotelAndSave(hotelsCaribbeanTsv, 'Caribbean')
    numHotels = numHotels + numHotelsOk

    hotelsMiddleAmericaTsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'Hotels_MiddleAmerica_1.tsv')
    numHotelsOk = parseHotelAndSave(hotelsMiddleAmericaTsv, 'Middle America')
    numHotels = numHotels + numHotelsOk

    hotelsMiddleEastTsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'Hotels_MiddleEast_1.tsv')
    numHotelsOk = parseHotelAndSave(hotelsMiddleEastTsv, 'Middle East')
    numHotels = numHotels + numHotelsOk

    hotelsSouthAmericaTsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'Hotels_SouthAmerica_1.tsv')
    numHotelsOk = parseHotelAndSave(hotelsSouthAmericaTsv, 'South America')
    numHotels = numHotels + numHotelsOk

    hotelsSouthAmerica2Tsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'Hotels_SouthAmerica_2.tsv')
    numHotelsOk = parseHotelAndSave(hotelsSouthAmerica2Tsv, 'South America 2')
    numHotels = numHotels + numHotelsOk

    hotelsNorthAmericaTsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'Hotels_NorthAmerica_1.tsv')
    numHotelsOk = parseHotelAndSave(hotelsNorthAmericaTsv, 'North America')
    numHotels = numHotels + numHotelsOk

    hotelsNorthAmerica2Tsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'Hotels_NorthAmerica_2.tsv')
    numHotelsOk = parseHotelAndSave(hotelsNorthAmerica2Tsv, 'North America 2')
    numHotels = numHotels + numHotelsOk

    hotelsNorthAmerica3Tsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'Hotels_NorthAmerica_3.tsv')
    numHotelsOk = parseHotelAndSave(hotelsNorthAmerica3Tsv, 'North America 3')
    numHotels = numHotels + numHotelsOk

    hotelsEuropeTsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'Hotels_Europe_1.tsv')
    numHotelsOk = parseHotelAndSave(hotelsEuropeTsv, 'Europe')
    numHotels = numHotels + numHotelsOk

    hotelsEurope2Tsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'Hotels_Europe_2.tsv')
    numHotelsOk = parseHotelAndSave(hotelsEurope2Tsv, 'Europe 2')
    numHotels = numHotels + numHotelsOk

    hotelsEurope3Tsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'Hotels_Europe_3.tsv')
    numHotelsOk = parseHotelAndSave(hotelsEurope3Tsv, 'Europe 3')
    numHotels = numHotels + numHotelsOk

    hotelsEurope4Tsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'Hotels_Europe_4.tsv')
    numHotelsOk = parseHotelAndSave(hotelsEurope4Tsv, 'Europe 4')
    numHotels = numHotels + numHotelsOk

    hotelsEurope5Tsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'Hotels_Europe_5.tsv')
    numHotelsOk = parseHotelAndSave(hotelsEurope5Tsv, 'Europe 5')
    numHotels = numHotels + numHotelsOk

    hotelsEurope6Tsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'Hotels_Europe_6.tsv')
    numHotelsOk = parseHotelAndSave(hotelsEurope6Tsv, 'Europe 6')
    numHotels = numHotels + numHotelsOk

    hotelsEurope7Tsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'Hotels_Europe_7.tsv')
    numHotelsOk = parseHotelAndSave(hotelsEurope7Tsv, 'Europe 7')
    numHotels = numHotels + numHotelsOk

    hotelsEurope8Tsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'Hotels_Europe_8.tsv')
    numHotelsOk = parseHotelAndSave(hotelsEurope8Tsv, 'Europe 8')
    numHotels = numHotels + numHotelsOk

    hotelsEurope9Tsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'Hotels_Europe_9.tsv')
    numHotelsOk = parseHotelAndSave(hotelsEurope9Tsv, 'Europe 9')
    numHotels = numHotels + numHotelsOk

    hotelsEurope10Tsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'Hotels_Europe_10.tsv')
    numHotelsOk = parseHotelAndSave(hotelsEurope10Tsv, 'Europe 10')
    numHotels = numHotels + numHotelsOk

    hotelsEurope11Tsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'Hotels_Europe_11.tsv')
    numHotelsOk = parseHotelAndSave(hotelsEurope11Tsv, 'Europe 11')
    numHotels = numHotels + numHotelsOk

    hotelsEurope12Tsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'Hotels_Europe_12.tsv')
    numHotelsOk = parseHotelAndSave(hotelsEurope12Tsv, 'Europe 12')
    numHotels = numHotels + numHotelsOk

    hotelsEurope13Tsv = os.path.join('/home/fer/PycharmProjects/HotelAdvisor/data', 'Hotels_Europe_13.tsv')
    numHotelsOk = parseHotelAndSave(hotelsEurope13Tsv, 'Europe 13')
    numHotels = numHotels + numHotelsOk

    endProcess = time.time()

    print(endProcess - startProcess)