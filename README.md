## BigConnect with SpringBoot 2.x

This is a sample project on how to use BigConnect inside a Spring Boot 2.x application. 

The example uses the Accumulo graph store, but you can use RocksDB as well. 
Just update the ```getGraphAuthorizationRepository()``` method inside the [BigConnectConfig.java](src/main/java/io/bigconnect/springbootexample/config/BigConnectConfig.java)
to return a ```com.mware.core.model.user.InMemoryGraphAuthorizationRepository```

#### Be sure to update ```src/main/resources/bc.properties``` to match your graph store config
