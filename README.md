# Asset Store PoC

This proof of concept demonstrates how to store, validate, and publish structured "assets" (for example CRE or SHIP deals).
It is a service that leans heavily on JSON Schema and JSLT so that new asset types and event payloads can be
introduced by dropping files on the classpath or definig asset structure in DB with minimal needs to shipping new Java code.

## Architectural overview

The code base follows a classic layered architecture:

* **HTTP layer** – REST controllers accept JSON requests and return JSON responses.
* **Domain layer** – services, models, and validators that implement the business rules.
* **Infrastructure layer** – persistence, mapping, and file-based registries that keep the system backed by a database and
  reusable configuration files.

### Main classes

| Component | Description |
| --- | --- |
| `AssetController` | Exposes the `/assets` API for create, bulk create, search, read, update, JSON Merge Patch, bulk patch, and delete operations. It delegates work to the command and query services without keeping state itself. |
| `EventController` | Provides `/events/{assetId}/{eventName}` to transform stored assets into event payloads using JSLT templates and optional schema validation before streaming the JSON back to the caller. |
| `AssetCommandServiceImpl` | Implements the write-side logic: creating assets, applying patches, maintaining links between assets, and performing soft deletes while logging each successful command. |
| `AssetQueryServiceImpl` | Offers read access by loading a single asset by id or searching with dynamic JPA specifications built from the request criteria. |
| `TypeSchemaRegistry` | Scans `schemas/types/*.schema.json`, compiles them with the networknt JSON Schema library, and keeps a registry of which `AssetType` values have associated schemas. |
| `AttributeDefinitionRegistry` | Reads the schemas discovered above and prepares attribute metadata (name, type, required flag) for each asset type so that other services can validate attribute payloads. |
| `AttributeDefinitionsBootstrapService` | Runs on application startup to persist any missing attribute definitions, ensuring the repositories can rely on the configuration found in the schema files. |
| `StartupInfoService` | Listens for the `ApplicationReadyEvent`, triggers attribute bootstrapping, and logs all discovered asset types together with the available event templates for visibility. |
| `JsonTransformer` | Loads and caches `transforms/events/*.jslt`, applies them to JSON payloads, and validates the result against optional output schemas while producing helpful error messages when template execution fails. |
| `JsonSchemaValidator` | Wraps the networknt validator to parse JSON, compile schemas, and translate validation failures into human-readable error messages. |
| `Asset` domain model | Represents an asset with core fields and a typed attribute collection, exposing helper methods to read and update attributes without revealing internal storage details. |
| `AssetMapper` & `AttributeMapper` | Use MapStruct to translate between domain objects and JPA entities, handling the soft-delete flag and converting typed attribute values into database-friendly structures. |

## Functional overview

### Current capabilities

* **Create and update assets** – The `/assets` controller supports single and bulk creation, full updates, JSON Merge Patch, and soft deletion by id.
* **Search and read** – Clients can list all assets or fetch one asset by id. Searches use criteria objects that are translated into JPA specifications before hitting the database.
* **Generate events** – `/events/{assetId}/{eventName}` turns an asset into event JSON using the configured JSLT templates and optional schema checks.
* **Schema-driven validation** – Asset schemas are discovered at startup, translated into attribute definitions, persisted, and reused for validation and database integrity checks.
* **Typed attribute handling** – Assets keep attributes as typed objects (String, Decimal, Boolean). The mapper layer converts them to and from the database automatically, which keeps the domain logic clean.

### Opportunities for further development

* **Pagination and richer filters** – `GET /assets` currently returns the full list without paging or query parameters. Extending the controller and search service with pagination and richer filtering would improve usability and performance for large datasets.
* **HTTP endpoints for asset links** – The command service already manages asset-to-asset links, but no controller exposes these capabilities. Dedicated endpoints would allow clients to manage relationships through the API.
* **Event error handling** – When event generation fails (for example due to malformed templates), the API throws a generic runtime exception. Returning a structured error response would make debugging easier for clients.

