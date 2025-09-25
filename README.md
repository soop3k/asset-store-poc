Asset Store PoC

This proof of concept shows how to store, validate, and publish assets such as CRE or SHIP deals. It is a Spring Boot service that relies on JSON Schemas and JSLT templates, so new asset types and event payloads can be introduced by adding files instead of Java code. Attribute definitions can be bootstrapped from JSON schemas or loaded directly from the database, which lets teams evolve the model without redeploying the service.

Architectural overview
The application follows a hexagonal arrangement. Domain services stay at the centre. REST controllers and scheduled jobs act as inbound adapters while database repositories, schema registries, and message builders sit on the outbound side.

Main classes
AssetController exposes the /assets endpoints for create, bulk create, search, read, update, patch, bulk patch, and delete requests.
EventController loads an asset, selects the right JSLT template, and returns the generated event payload.
AssetCommandServiceImpl runs the command side. It handles create, patch, delete, and link management while keeping a log and applying soft deletes.
AttributeDefinitionsBootstrapService stores missing attribute definitions at startup so attributes can be introduced through schema files or database seeding.
AssetQueryServiceImpl reads assets by id or by search criteria that translate into JPA specifications.
TypeSchemaRegistry scans classpath schemas, compiles them, and records which asset types provide schema files.
AttributeDefinitionRegistry parses schemas and builds the catalogue of attribute definitions for each asset type.
StartupInfoService triggers the bootstrap step after startup and reports available asset types together with the JSLT templates that were found.
JsonTransformer loads and caches event templates, applies them to payloads, and validates the output when a schema is present.
JsonSchemaValidator wraps the JSON Schema validator and turns failures into readable error messages.
The Asset domain model represents an asset with core fields and a collection of typed attributes, exposing helper methods for safe updates.
AssetMapper and AttributeMapper convert between domain objects and JPA entities while handling the soft delete flag and attribute typing.

Functional overview
The system already allows create, update, patch, and delete operations through the REST API, including bulk operations. It can search and read assets by turning criteria objects into JPA specifications before hitting the database. Dynamic attributes are managed because schemas add new attributes and the bootstrap step also accepts definitions inserted straight into the database. The service maintains links between assets so relationships can be stored with metadata and revisited later. It generates events by calling /events/{assetId}/{eventName}, which applies the configured JSLT template and optional schema validation. Asset payloads are validated with the schemas that were discovered and persisted at startup. Typed attributes are handled inside the domain model while the mapper layer translates values to and from the database.

Outstanding work includes pagination and richer filters for GET /assets because the current implementation returns the full list without query parameters. HTTP endpoints are still needed to expose the link management already supported by the command service. Event generation also needs better error handling so clients receive structured responses instead of generic runtime exceptions.
