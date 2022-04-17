package com.bangular.main.graphql;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@Component
public class GraphQLProvider {

    // Initializes the GraphQL configuration and Handles Requests from the clients

    private static final String GRAPHQL_SCHEMA_FILE = "/schema.graphqls";

    private GraphQL graphQL;

    @Autowired
    private GraphQLDataFetcher graphQLDataFetcher;

    @Bean
    public GraphQL graphQL() {
        return graphQL;
    }

    @PostConstruct
    public void init() throws URISyntaxException, IOException {
        Path graphQLSchemaFilePath = Paths.get(this.getClass().getResource(GRAPHQL_SCHEMA_FILE).toURI());;
        GraphQLSchema graphQLSchema = buildSchema(graphQLSchemaFilePath);
        this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    }

    private GraphQLSchema buildSchema(Path graphQLSchemaFilePath) throws IOException {
        String graphQLSchema = new String(Files.readAllBytes(graphQLSchemaFilePath), StandardCharsets.UTF_8);
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(graphQLSchema);
        RuntimeWiring runtimeWiring = buildWiring();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }

    private RuntimeWiring buildWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("Query")
                        .dataFetcher("getUsers", graphQLDataFetcher.getUsers()))
                .build();
    }
}
