package com.bangular.main.graphql;

import com.bangular.main.services.UserServiceImpl;
import graphql.GraphQL;
import graphql.Scalars;
import graphql.schema.*;
import graphql.schema.idl.*;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@Component
@RequiredArgsConstructor
public class GraphQLProvider {

    private static final String GRAPHQL_SCHEMA_FILE = "/schema.graphql";
    private GraphQL graphQL;
    private GraphQLSchema graphQLSchema;
    private final GraphQLDataFetcher graphQLDataFetcher;
    public DatabaseMetaData databaseMetaData;
    public ResultSet resultSet = null;
    public ArrayList<String> tables = null;
    public SchemaPrinter schemaPrinter = new SchemaPrinter();
    @Value("${spring.datasource.url}")
    private String connectionUrl;
    @Value("${spring.datasource.username}")
    private String connectionUsername;
    @Value("${spring.datasource.password}")
    private String connectionPassword;

    private final UserServiceImpl userService;

    @Bean
    public GraphQL graphQL() {
        return graphQL;
    }

    @PostConstruct
    public void init() throws SQLException, IOException, URISyntaxException {
        Path graphQLSchemaFilePath = Paths.get(
                Objects.requireNonNull(this.getClass().getResource(GRAPHQL_SCHEMA_FILE)).toURI());
        GraphQLSchema localSchema = buildSchema(graphQLSchemaFilePath);
        graphQLSchema = localSchema;
        this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
//        getVisitorSub(schema);

        databaseMetaData = getDBMetaData();
        schemaTranformation();

    }

    private GraphQLTypeVisitorStub getVisitorSub(GraphQLObjectType graphQLObjectType) {
        return new GraphQLTypeVisitorStub() {
            @Override
            public TraversalControl visitGraphQLObjectType(GraphQLObjectType objectType,
                                                           TraverserContext<GraphQLSchemaElement> context) {
                GraphQLCodeRegistry.Builder codeRegistryBuilder = context.getVarFromParents(
                        GraphQLCodeRegistry.Builder.class);
//                if(objectType.hasAppliedDirective("specialDirective")) {
//                GraphQLObjectType newObjectType = buildChangedObjectType(objectType, codeRegistryBuilder);

                DataFetcher newDataFetcher = dataFetchingEnvironment -> {
                  return userService.healthCheck();
                };
                FieldCoordinates fieldCoordinates = FieldCoordinates.coordinates(
                        Objects.requireNonNull(objectType.getName()),
                        String.valueOf(objectType.getFields()));
                codeRegistryBuilder.dataFetcher(fieldCoordinates, newDataFetcher);

                return insertAfter(context, graphQLObjectType);
//                return changeNode(context, graphQLObjectType);
//                }
//                return TraversalControl.CONTINUE;
            }

//            public GraphQLObjectType buildChangedObjectType(GraphQLObjectType objectType,
//                                                            GraphQLCodeRegistry.Builder graphQLCodeRegistry) {
//                GraphQLFieldDefinition newField = GraphQLFieldDefinition.newFieldDefinition()
//                        .name("ufuk").type(Scalars.GraphQLString).build();
//
//                GraphQLObjectType newObjectType = objectType.transform(builder -> builder.field(newField));
//
//                DataFetcher newDataFetcher = dataFetchingEnvironment -> {
//                    return userService.getUser("dustbreaker");
//                };
//
////                objectType.getName(), newField.getName()
//                FieldCoordinates.
//                FieldCoordinates coordinates = FieldCoordinates.coordinates(objectType.getName(),objectType.getFields());
//                graphQLCodeRegistry.dataFetcher(coordinates, newDataFetcher);
//                return newObjectType;
//            }
        };

//        graphQLSchema = SchemaTransformer.transformSchema(schema, visitorStub);

//        this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    }

    public void schemaTranformation() throws SQLException {
        resultSet = databaseMetaData.getTables(null, null, null, new String[]{"TABLE"});
        tables = new ArrayList();

        while (resultSet.next()) {
            tables.add(resultSet.getString("TABLE_NAME"));
        }

        List<Map<String, GraphQLObjectType>> createdSchema = new ArrayList<>();
        for (String table : tables) {
            createdSchema.add(createSchema(table));
        }
        System.out.println(createdSchema);
//        createdSchema.forEach(map -> map.entrySet().forEach(value -> {
////            graphQLSchema.transform(builder -> builder.query(value.getValue())); // WARNING: It does not do what we want
////            SchemaTransformer.transformSchema(graphQLSchema, visitorStub);
//        }));
        for (Map<String, GraphQLObjectType> stringGraphQLObjectTypeMap : createdSchema) {
            for (Map.Entry<String, GraphQLObjectType> stringGraphQLObjectTypeEntry : stringGraphQLObjectTypeMap.entrySet()) {
                GraphQLTypeVisitorStub visitorSub = getVisitorSub(stringGraphQLObjectTypeEntry.getValue());
                SchemaTransformer.transformSchema(graphQLSchema, visitorSub);
            }
        }

        this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    }

    public Map<String, GraphQLObjectType> createSchema(String tableName) throws SQLException {
        Map<String, GraphQLObjectType> returnedMap = new HashMap<>();

        resultSet = databaseMetaData.getColumns(null, null, (String) tableName, null);
        GraphQLObjectType.Builder builder = GraphQLObjectType.newObject();

        while (resultSet.next()) {
            builder
                    .name(tableName.toLowerCase())
                    .field(GraphQLFieldDefinition.newFieldDefinition()
                            .name(resultSet.getString("COLUMN_NAME"))
                            .type(GraphQLNonNull.nonNull(ReturnType(resultSet.getString("TYPE_NAME")))))
                    .build();
        }

        returnedMap.put(schemaPrinter.print(builder.build()), builder.build());
        return returnedMap;
    }

    private GraphQLSchema buildSchema(Path graphQLSchemaFilePath) throws IOException {
        String graphQLSchema = Files.readString(graphQLSchemaFilePath);
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(graphQLSchema);
        RuntimeWiring runtimeWiring = buildWiring();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }

    private RuntimeWiring buildWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("Query")
                        .dataFetcher("getUsers", graphQLDataFetcher.getUsers())
                        .dataFetcher("getUserByUsername", graphQLDataFetcher.getUserByUsername()))
                .build();
    }

    public DatabaseMetaData getDBMetaData() throws SQLException {
        Connection connection = DriverManager.getConnection(connectionUrl, connectionUsername, connectionPassword);
        return connection.getMetaData();
    }

    public static GraphQLScalarType ReturnType(@NotNull String type) {
        // Translate postgres types to graphql scalar types
        if (type.equals("int8")) {
            return Scalars.GraphQLInt;
        } else if (type.equals("varchar")) {
            return Scalars.GraphQLString;
        } else {
            return Scalars.GraphQLString;
        }
    }
}
