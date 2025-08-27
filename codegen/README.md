Sample CURL to run the code generation endpoint

```
curl --location 'http://localhost:8080/api/codegen/generate' \
--header 'Content-Type: application/json' \
--data '{
    "rootPackage":"com.codegen.project",
    "definitions": {
        "Books" : {
            "properties" : {
                "id": {
                    "type": "long"
                },
                "bookName": {
                    "type": "string"
                },
                "bookQty": {
                    "type": "integer"
                }
            }
        }
    }
}'
```
