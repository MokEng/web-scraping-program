package se.miun.dt002g.webscraper.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.event.ServerHeartbeatFailedEvent;
import com.mongodb.event.ServerHeartbeatStartedEvent;
import com.mongodb.event.ServerHeartbeatSucceededEvent;
import com.mongodb.event.ServerMonitorListener;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class MongoDbHandler {
    com.mongodb.client.MongoClient client;
    public String getConnectionString() {
        return connectionString;
    }

    String connectionString = null;
    boolean isConnected = false;

    public boolean tryConnect(String connectionString){
        if(client!=null){
            client.close();
        }
        try{
            client = MongoClients.create(MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(connectionString))
                    .applyToServerSettings(builder -> builder.addServerMonitorListener(new ServerMonitorListener() {
                        @Override
                        public void serverHearbeatStarted(ServerHeartbeatStartedEvent event) {
                            isConnected = true;
                            System.out.println("isConnected = true @1");
                        }

                        @Override
                        public void serverHeartbeatSucceeded(ServerHeartbeatSucceededEvent event) {
                            isConnected = true;
                            System.out.println("isConnected = true @2");
                        }

                        @Override
                        public void serverHeartbeatFailed(ServerHeartbeatFailedEvent event) {
                            isConnected = false;
                            System.out.println("isConnected = false @3");
                        }
                    })).build());
            // Temporary until I figure out how to check if credentials are OK.
            MongoDatabase database = client.getDatabase("test");
            MongoCollection<Document> coll = database.getCollection("test");
            coll.drop();

        }catch(Exception e ){
            e.printStackTrace();
            if(client != null){
                client.close();
            }
            client = null;
            this.connectionString = "";
            isConnected = false;
            return false;
        }
        if(isConnected){
            this.connectionString = connectionString;
        }
        return true;
    }

    public boolean isConnected(){
        return isConnected;
    }

    public boolean storeJsonInDb(DbStorageSettings settings, List<String> jsonStrings){

        try{
            MongoDatabase database = client.getDatabase(settings.databaseName);
            MongoCollection<Document> coll = database.getCollection(settings.collectionName);
            if(settings.dropPreviousData){ // drop data already stored in collection
                coll.drop();
            }
            //Bulk Approach:
            int count = 0;
            int batch = 100;
            List<InsertOneModel<Document>> docs = new ArrayList<>();

            for(String s : jsonStrings){
                docs.add(new InsertOneModel<>(Document.parse(s)));
                count++;
                if (count == batch) { // write every 100:th document to collection
                    coll.bulkWrite(docs, new BulkWriteOptions().ordered(false));
                    docs.clear();
                    count = 0;
                }
            }
            if (count > 0) { // write remaining documents
                BulkWriteResult bulkWriteResult=  coll.bulkWrite(docs, new BulkWriteOptions().ordered(false));
                System.out.println("Inserted" + bulkWriteResult);
            }
        }catch(Exception e){
            e.printStackTrace();
            client = null;
            return false;
        }
        return true;
    }
}
