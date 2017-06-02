package es.pfm.hoteladvisor.search;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import java.net.InetAddress;

public class ElasticSearch {

    private TransportClient client;

    public TransportClient getClient()
    {
        if(client == null)
        {
            client = createClient();
        }

        return client;
    }

    protected TransportClient createClient()
    {
        if(client == null)
        {
            try {
                client = new PreBuiltTransportClient(Settings.EMPTY).addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"),9300));
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
        }
        return client;
    }

}
