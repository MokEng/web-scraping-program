package se.miun.dt002g.webscraper.Database;

import com.mongodb.MongoWriteException;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.InsertOneModel;
import org.bson.Document;
import se.miun.dt002g.webscraper.scraper.DataHandler;
import se.miun.dt002g.webscraper.scraper.Pair;
import se.miun.dt002g.webscraper.scraper.Sitemap;

import java.util.ArrayList;
import java.util.List;

public class MongoDbHandler {
    com.mongodb.client.MongoClient client;

    public boolean tryConnect(String connectionString){
        try{
            client = MongoClients.create(connectionString);
        }catch(Exception e ){
            e.printStackTrace();
            return false;
        }
        return true;
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
            return false;
        }
        return true;
    }
}
