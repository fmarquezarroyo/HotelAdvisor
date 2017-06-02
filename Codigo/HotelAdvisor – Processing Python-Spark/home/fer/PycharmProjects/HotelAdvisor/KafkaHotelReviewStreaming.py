#!/usr/bin/env python
# -*- coding: utf-8 -*-

from __future__ import print_function

from pyspark import SparkContext
from pyspark.streaming import StreamingContext
from pyspark.streaming.kafka import KafkaUtils
from pyspark.sql import Row
from pymongo import MongoClient
import json
import pprint

#def parseReview(review):
    #row = Row(
        #date=review["date"],
        #hotelId = review["hotel_id"],
        #username = review["username"],
        #location=review["location"],
        #cleanliness=review["cleanliness"],
        #confort=review["confort"],
        #installations=review["installations"],
        #personal=review["personal"],
        #wifi=review["wifi"],
        #valueMoney=review["valueMoney"],
        #lang=review["lang"],
        #comment=review["comment"]
    #)

    #return row

#def insertHotelReviewsMongoDb(hotelReviewRow):

    #hotel = db.hotels.find_one({"id": hotelReviewRow.hotelId})

    #pprint.pprint(hotel)

    #print("Updating hotel in database: %s" % hotel)

    #db.hotels.update(hotel)


def insertHotelReviewsMongoDb(partition):
    client = MongoClient()
    db = client['HotelAdvisor']
    hotelsColl = db.get_collection('hotels')

    for review in partition:
        #print("Review: %s" % review)

        date = review["date"],
        age = int(review["age"])
        hotelId = review["hotel_id"]
        username = review["username"]
        # print("Username: %s" % username)
        location = round(float(review["location"]),1)
        #print("Location: %f" % location)
        cleanliness = round(float(review["cleanliness"]),1)
        confort = round(float(review["confort"]),1)
        installations = round(float(review["installations"]),1)
        personal = round(float(review["personal"]),1)
        wifi = round(float(review["wifi"]),1)
        valueMoney = round(float(review["valueMoney"]),1)
        lang = review["lang"]
        comment = review["comment"]
        totalScore= round((location+cleanliness+confort+installations+personal+wifi+valueMoney)/7,1)

        hotelCursor = hotelsColl.find({"id": hotelId})
        #pprint.pprint(hotelCursor)
        for hotel in hotelCursor:
            oldNumReviews = hotel["review_nr"]
            if (oldNumReviews==0):
                reviews = []
            else:
                reviews = hotel["reviews"]

            #pprint.pprint(reviews)

            rev = {
                "date": date[0],
                "username": username,
                "age": age,
                 #"hotel_id": hotelId,
                "location": location,
                "cleanliness": cleanliness,
                "confort": confort,
                "installations": installations,
                "personal": personal,
                "wifi": wifi,
                "valueMoney": valueMoney,
                "lang": lang,
                "comment": comment,
                "score": totalScore
            }

            reviews.append(rev)
            #pprint.pprint(reviews)

            newNumReviews = oldNumReviews + 1
            # pprint.pprint(newNumReviews)

            oldScore = hotel["review_score"]
            newScore = ((oldScore*oldNumReviews)+ totalScore)/newNumReviews

            hotelsColl.update({"id": hotelId}, {"$set": {"reviews": reviews, "review_nr": newNumReviews, "review_score":newScore}}, upsert=True)

    client.close()


if __name__ == "__main__":

    sc = SparkContext(appName="HotelReviewStreaming")
    ssc = StreamingContext(sc, 1)

    #client = MongoClient()
    #db = client['HotelAdvisor']

    ssc.checkpoint("/home/fer/spark_streaming_hotel_reviews.chk")

    brokers = "localhost:9092"
    topic = "hotel_reviews"
    hotelReviewDS = KafkaUtils.createDirectStream(ssc, [topic], {"metadata.broker.list": brokers})
    hotelReviewDS.pprint()
    hotelReviewDS = hotelReviewDS.map(lambda (none, review): review)
    #hotelReviewDS.pprint()
    hotelReviewDS = hotelReviewDS.map(lambda review: json.loads(review))
    #hotelReviewDS.pprint()
    #hotelReviewDS = hotelReviewDS.map(parseReview).cache()
    #hotelReviewDS.pprint()

    hotelReviewDS.foreachRDD(lambda rdd: rdd.foreachPartition(insertHotelReviewsMongoDb))

    ssc.start()
    ssc.awaitTermination()