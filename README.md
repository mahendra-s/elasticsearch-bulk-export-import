# elastic search bulk export import utility

### Run Export data from ES to Local data dictionary :
 ```
    $sbt "runMain com.example.es.ExtractProcessor <index name>"
 ```

### Run Import data into ES cluster from local data dictionary:
```
    $sbt "runMain com.example.es.ImportProcessor <data directory> <index name>"
```
### Example
```
 $sbt "runMain com.example.es.ExtractProcessor product"

 $sbt "runMain com.example.es.ImportProcessor data/product product"
```
