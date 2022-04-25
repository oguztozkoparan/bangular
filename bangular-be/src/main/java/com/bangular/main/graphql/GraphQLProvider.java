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

    private static final String GRAPHQL_SCHEMA_FILE = "/schema.graphqls";
    private GraphQL graphQL;
    private GraphQLSchema graphQLSchema;
    private final GraphQLDataFetcher graphQLDataFetcher;
    private DatabaseMetaData databaseMetaData;
    private ResultSet resultSet = null;
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
        this.graphQLSchema = localSchema;
        this.graphQL = GraphQL.newGraphQL(this.graphQLSchema).build();

        databaseMetaData = getDBMetaData();
        schemaTransformation();
    }

    private GraphQLTypeVisitorStub getVisitorSub(GraphQLObjectType graphQLObjectType) {
        return new GraphQLTypeVisitorStub() {
            @Override
            public TraversalControl visitGraphQLObjectType(GraphQLObjectType objectType,
                                                           TraverserContext<GraphQLSchemaElement> context) {
                GraphQLCodeRegistry.Builder codeRegistryBuilder = context.getVarFromParents(
                        GraphQLCodeRegistry.Builder.class);
                List<GraphQLFieldDefinition> graphQLFieldDefinitions = new ArrayList<>(graphQLObjectType.getFields());
//                GraphQLObjectType newObjectType = objectType.transform(builder -> builder
//                        .name(graphQLObjectType.getName())
//                        .fields(graphQLObjectType.getFieldDefinitions())
//                        .build());
                DataFetcher newDataFetcher = dataFetchingEnvironment -> {
                    return userService.healthCheck();
                };

                FieldCoordinates fieldCoordinates = null;
                for (GraphQLFieldDefinition fieldDefinition : graphQLFieldDefinitions) {
                    fieldCoordinates = FieldCoordinates.coordinates(
                            objectType.getName(), fieldDefinition.getName());
                    codeRegistryBuilder.dataFetcher(fieldCoordinates, newDataFetcher);
                }

//                return insertAfter(context, newObjectType);
                return changeNode(context, graphQLObjectType);
            }
        };
    }

    public void schemaTransformation() throws SQLException {
        resultSet = databaseMetaData.getTables(null, null, null, new String[]{"TABLE"});
        tables = new ArrayList<>();

        while (resultSet.next()) {
            tables.add(resultSet.getString("TABLE_NAME"));
        }

        List<List<GraphQLObjectType>> createdSchemas = new ArrayList<>();
        for (String table : tables) {
            createdSchemas.add(createSchema(table));
        }
//        System.out.println(createdSchemas);
        for (List<GraphQLObjectType> graphQLObjectTypeList : createdSchemas) {
            for (GraphQLObjectType graphQLObjectType : graphQLObjectTypeList) {
                GraphQLTypeVisitorStub visitorSub = getVisitorSub(graphQLObjectType);
                this.graphQLSchema = SchemaTransformer.transformSchema(this.graphQLSchema, visitorSub);
            }
        }
        this.graphQL = GraphQL.newGraphQL(this.graphQLSchema).build();
    }

    public List<GraphQLObjectType> createSchema(String tableName) throws SQLException {
        List<GraphQLObjectType> objectTypes = new ArrayList<>();

        resultSet = databaseMetaData.getColumns(null, null, (String) tableName, null);
        GraphQLObjectType.Builder builder = GraphQLObjectType.newObject();

        while (resultSet.next()) {
            builder
                    .name(tableName.toLowerCase())
                    .field(GraphQLFieldDefinition.newFieldDefinition()
                            .name(resultSet.getString("COLUMN_NAME"))
                            .type(GraphQLNonNull.nonNull(returnType(resultSet.getString("TYPE_NAME")))))
                    .build();
        }

        objectTypes.add(builder.build());
        return objectTypes;
    }

    private GraphQLSchema buildSchema(Path graphQLSchemaFilePath) throws IOException {
        String graphQLSchemaString = Files.readString(graphQLSchemaFilePath);
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(graphQLSchemaString);
        RuntimeWiring runtimeWiring = buildWiring();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }

    private RuntimeWiring buildWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("Query")
                        .dataFetcher("getUsers", graphQLDataFetcher.getUsers())
                        .dataFetcher("getUserByUsername", graphQLDataFetcher.getUserByUsername()))
                .type(newTypeWiring("User"))
                .build();
    }

    public DatabaseMetaData getDBMetaData() throws SQLException {
        Connection connection = DriverManager.getConnection(connectionUrl, connectionUsername, connectionPassword);
        return connection.getMetaData();
    }

    public static GraphQLScalarType returnType(@NotNull String type) {
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
