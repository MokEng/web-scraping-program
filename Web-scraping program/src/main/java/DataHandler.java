import com.google.gson.Gson;
import org.openqa.selenium.json.Json;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                    allTasks.add((TextTask) temp);
                }
                temp = temp.getDoFirst();
            }
        });
        return allTasks;
    }

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
     * Converts grouped data from a sitemap to JSON-format.
     * @param groupby = what to group the data by
     * @param sitemap = The sitemap which holds the tasks
     * @return a list of strings in JSON format.
     */
    public static List<String> toJSON(GROUPBY groupby, Sitemap sitemap){
        List<String> jsons= new ArrayList<>();
        groupTextTasksBy(groupby,sitemap).forEach((s, textTasks) -> {
            Data d = new Data(s,textTasks.stream().map(t->new TaskData(t.id,t.dataName,t.data)).toList());
            jsons.add(new Gson().toJson(d));
        });
        return jsons;
    }







}
