package es.pfm.hoteladvisor.search;

import org.apache.lucene.search.TermQuery;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder.*;

public class ElasticSearchQueryHelper {

    public static SearchResponse rangeQuery(String type, SearchType searchType, String field, Integer fromValue, Integer toValue) {

        TransportClient client = new ElasticSearch().getClient();

        SearchResponse response = client.prepareSearch("hoteladvisor")
            .setTypes(type)
            .setSearchType(searchType)
            .setPostFilter(QueryBuilders.rangeQuery(field)
                    .from(fromValue)
                    .to(toValue))
            .execute()
            .actionGet();

    return response;
    }

    public static SearchResponse elementQuery(String elementName, String elementField, String type, SearchType searchType, int size){
        TransportClient client = new ElasticSearch().getClient();
        QueryStringQueryBuilder stringQueryName= QueryBuilders.queryStringQuery(elementName).field(elementField);

        SearchResponse response = client.prepareSearch("hoteladvisor")
                .setTypes(type)
                .setSearchType(searchType)
                .setQuery(stringQueryName)
                .setSize(size)
                .execute()
                .actionGet();

        return response;
    }

    public static SearchResponse fuzzyQuery(String elementName, String elementField, String type, SearchType searchType, int size, int fuzziness) {

        TransportClient client = new ElasticSearch().getClient();
        FuzzyQueryBuilder stringQueryName = QueryBuilders.fuzzyQuery(elementField, elementName).fuzziness(Fuzziness.build(fuzziness));

        SearchResponse response = client.prepareSearch("hoteladvisor")
                .setTypes(type)
                .setSearchType(searchType)
                .setQuery(stringQueryName)
                .setSize(size)
                .execute()
                .actionGet();


        return response;
    }



    public static SearchResponse moreLikeThisQuery(String continentName, String countryName, String cityName, String hotelName, double review_score, int step_score, double clazz, int step_clazz, double maxRate, int step_maxRate, int minimunShouldMatch, int maxRecommendations, String type, SearchType searchType) {

        TransportClient client = new ElasticSearch().getClient();

        //TermQueryBuilder termQueryContinent = QueryBuilders.termQuery("continent_id", continentName.toLowerCase());
        //TermQueryBuilder termQueryCountry = QueryBuilders.termQuery("cc1", countryName.toLowerCase());
        //TermQueryBuilder termQueryCity = QueryBuilders.termQuery("city_hotel", cityName.toLowerCase());


        //TermQuery doesn't work with string fields with spaces (blanks)
        QueryStringQueryBuilder stringQueryContinent= QueryBuilders.queryStringQuery(continentName).field("continent_id");
        QueryStringQueryBuilder stringQueryCountry= QueryBuilders.queryStringQuery(countryName).field("cc1");
        QueryStringQueryBuilder stringQueryCity= QueryBuilders.queryStringQuery(cityName).field("city_hotel");
        QueryStringQueryBuilder stringQueryName= QueryBuilders.queryStringQuery(hotelName).field("name");

        RangeQueryBuilder rangeQueryScore = QueryBuilders.rangeQuery("review_score").from(review_score-step_score,true).to(review_score+step_score,true).boost(2);
        RangeQueryBuilder rangeQueryClazz = QueryBuilders.rangeQuery("class").from(clazz-step_clazz,true).to(clazz+step_clazz,true).boost(3);
        RangeQueryBuilder rangeQueryMaxRate = QueryBuilders.rangeQuery("min_rate_EUR").from(maxRate-step_maxRate,true).to(maxRate+step_maxRate,true);

        QueryBuilder qb = QueryBuilders
                .boolQuery()
                .must(stringQueryContinent)
                .must(stringQueryCountry)
                .must(stringQueryCity)
                .mustNot(stringQueryName) //Not include hotel to compare
                .should(rangeQueryScore)
                .should(rangeQueryClazz)
                .should(rangeQueryMaxRate)
                .minimumShouldMatch(minimunShouldMatch);

        SearchResponse response = client.prepareSearch("hoteladvisor")
                .setTypes(type)
                .setSearchType(searchType)
                .setQuery(qb)
                .setSize(maxRecommendations)
                //.addSort(SortBuilders.fieldSort("review_score").order(SortOrder.DESC)) //Results will be ordered by elastic search score
                .execute()
                .actionGet();

        return response;
    }

    public static SearchResponse matchPhraseQuery(String type, SearchType searchType, String name, String text, int slop) {

        TransportClient client = new ElasticSearch().getClient();

        MatchPhraseQueryBuilder mpqb;
        if(slop>0)
            mpqb = QueryBuilders.matchPhraseQuery(name, text).slop(slop);
        else
            mpqb = QueryBuilders.matchPhraseQuery(name, text);


        SearchResponse response = client.prepareSearch("hoteladvisor")
                .setTypes(type)
                .setSearchType(searchType)
                .setPostFilter(mpqb)
                .setSize(10000)
                .execute()
                .actionGet();

        return response;
    }

    public static SearchResponse advancedSearchHotelsQuery(String continentName, String countryName, String cityName, String review_score, String clazz, String maxRate, String type, SearchType searchType, String field, String text, int slop) {

        TransportClient client = new ElasticSearch().getClient();


        //TermQueryBuilder termQueryContinent = QueryBuilders.termQuery("continent_id", continentName.toLowerCase());
        //TermQueryBuilder termQueryCountry = QueryBuilders.termQuery("cc1", countryName.toLowerCase());
        //TermQueryBuilder termQueryCity = QueryBuilders.termQuery("city_hotel", cityName.toLowerCase());


        //TermQuery doesn't work with string fields with spaces (blanks)
        QueryStringQueryBuilder stringQueryContinent= QueryBuilders.queryStringQuery(continentName).field("continent_id");
        QueryStringQueryBuilder stringQueryCountry= QueryBuilders.queryStringQuery(countryName).field("cc1");
        QueryStringQueryBuilder stringQueryCity= QueryBuilders.queryStringQuery(cityName).field("city_hotel");

        RangeQueryBuilder rangeQueryScore = null;

        if (review_score.startsWith("+"))
            rangeQueryScore = QueryBuilders.rangeQuery("review_score").from(Double.parseDouble(review_score.substring(1, review_score.length())),false).to(10);
        else if (review_score.endsWith("+"))
            rangeQueryScore = QueryBuilders.rangeQuery("review_score").from(Double.parseDouble(review_score.substring(0,review_score.length()-1)),true).to(10);
        else
            rangeQueryScore = QueryBuilders.rangeQuery("review_score").from(Double.parseDouble(review_score)).to(Integer.parseInt(review_score));

        RangeQueryBuilder rangeQueryClazz = null;
        if (clazz.startsWith("+"))
            rangeQueryClazz = QueryBuilders.rangeQuery("class").from(Double.parseDouble(clazz.substring(1, clazz.length())),false);
        else if (clazz.endsWith("+"))
            rangeQueryClazz = QueryBuilders.rangeQuery("class").from(Double.parseDouble(clazz.substring(0, clazz.length()-1)),true);
        else
            rangeQueryClazz = QueryBuilders.rangeQuery("class").from(Double.parseDouble(clazz)).to(Integer.parseInt(clazz));

        RangeQueryBuilder rangeQueryMaxRate = QueryBuilders.rangeQuery("min_rate_EUR").to(Integer.parseInt(maxRate));

        QueryBuilder qb = QueryBuilders
                .boolQuery()
                .must(QueryBuilders.matchPhraseQuery(field, text).slop(slop))
                .must(stringQueryContinent)
                .must(stringQueryCountry)
                .must(stringQueryCity)
                .must(rangeQueryScore)
                .must(rangeQueryClazz)
                .must(rangeQueryMaxRate);

        SearchResponse response = client.prepareSearch("hoteladvisor")
                .setTypes(type)
                .setSearchType(searchType)
                .setQuery(qb)
                .setSize(10000)
                .addSort(SortBuilders.fieldSort("review_score").order(SortOrder.DESC))
                .execute()
                .actionGet();

        return response;
    }


    /*public static SearchResponse moreLikeThisQuery(String type, SearchType searchType, String[] fields, String[] texts, String id) {

        TransportClient client = new ElasticSearch().getClient();

        Item item = new Item("hoteladvisor",type,id);
        Item[] items = {item};

        MoreLikeThisQueryBuilder moreLikeThisQuery=QueryBuilders.moreLikeThisQuery(fields, texts, items).minDocFreq(1).minTermFreq(1);

        SearchResponse response = client.prepareSearch("hoteladvisor")
                .setTypes(type)
                .setSearchType(searchType)
                .setQuery(moreLikeThisQuery)
                .execute()
                .actionGet();

        return response;
    }*/

}
