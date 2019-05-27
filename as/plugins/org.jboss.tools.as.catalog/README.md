# The Server Tools Catalog project

## Updating the schema files

Once in this folder, run the following command:

```java
java -cp target/org.jboss.tools.as.catalog-*-SNAPSHOT.jar $SERVER_folder schema
```

where **$SERVER_folder** is the folder under the Wildfly or EAP distribution where the
schemas are stored. Generally this is the **docs/schema** sub folder.

Some files may be reported as changed but seems only the line ending is different, you can
skip them.

Some files may have been copied in both the dtd and xsd folders, so check and delete the
extra one.

If you are updating from several installations, please run the previous command in the
reverse update time order (ie latest released last)

## Updating the schema catalog

Once you've updated the schema files

