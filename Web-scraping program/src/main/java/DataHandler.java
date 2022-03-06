import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

enum GROUPBY{id,dataName};
public class DataHandler {
    /**
     * Get all tasks in sitemap, even the ones hidden with decorator pattern.
     * @return a list of data strings extracted from the web
     */
    private static List<TextTask> getAllTextTasks(Sitemap sitemap){
        List<TextTask> allTasks= new ArrayList<>();
        sitemap.getTasks().forEach(task->{
            Task temp = task;
            while (temp != null) {
                if(temp instanceof TextTask){
                    if(temp.exception==null)
                        allTasks.add((TextTask) temp);
                }
                temp = temp.getDoFirst();
            }
        });
        return allTasks;
    }

    /**
     * Groups Task-objects with a key-value
     * @param groupby, what variable to base the grouping on
     * @param sitemap, tasks are found in sitemap
     * @return a Map object which groups Task-objects to a key.
     */
    private static Map<String,List<TextTask>> groupTextTasksBy(GROUPBY groupby,Sitemap sitemap){
        List<TextTask> all = getAllTextTasks(sitemap);
        Map<String,List<TextTask>> grouped = new HashMap<>();
        if(groupby == GROUPBY.id){
            grouped = all.stream().collect(
                    Collectors.groupingBy(t->t.id));
        }else if(groupby == GROUPBY.dataName){
            grouped = all.stream().collect(
                    Collectors.groupingBy(t->t.dataName));
        }
        return grouped;
    }

    /**
     * Converts and groups data from Task objects to JSON-format.
     * @param groupby, what to group the data by
     * @param sitemap, The sitemap which holds the Task objects
     * @return a list of strings in JSON format.
     */
    public static List<String> toJSON(GROUPBY groupby, Sitemap sitemap){
        List<String> jsons= new ArrayList<>();
        groupTextTasksBy(groupby,sitemap).forEach((s, textTasks) -> {
            Data d = new Data(groupby.name(),textTasks.stream().map(t->new TaskData(t.id,t.dataName,t.data)).toList());
            jsons.add(new Gson().toJson(d));
        });
        return jsons;
    }

    /**
     * Writes and groups scraped data from Task-objects to a file in JSON format
     * @param groupby, what to group the data by
     * @param sitemap, the sitemap containing the tasks
     * @param filename, output file path
     * @return true if write was successful
     */
    public static boolean toJSONFile(GROUPBY groupby, Sitemap sitemap, String filename){
        try{
            FileWriter fileWriter = new FileWriter(filename);
            Map<String, List<TextTask>> map = groupTextTasksBy(groupby,sitemap);
            AtomicInteger s1 = new AtomicInteger(map.size());
            fileWriter.append('[');
            map.forEach((s, textTasks) -> {
                Data d = new Data(groupby.name(),textTasks.stream().map(t->new TaskData(t.id,t.dataName,t.data)).toList());
                new Gson().toJson(d,fileWriter);
                s1.addAndGet(-1);
                if(s1.get() != 0){
                    try {
                        fileWriter.append(',');
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            fileWriter.append(']');
            fileWriter.close();
        }catch(Exception e){
            return false;
        }
        return true;
    }







}
