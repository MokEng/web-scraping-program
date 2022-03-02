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

    public static Map<String,List<TextTask>> groupTextTasksBy(GROUPBY groupby,Sitemap sitemap){
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








}
