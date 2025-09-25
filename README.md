# Asset Store PoC

This proof of concept shows how to store, validate, and publish "assets" (for example CRE or SHIP deals). It is a Spring Boot
service built around JSON Schemas and JSLT templates, so new asset types and event payloads can be introduced by adding files
instead of Java code.

## Architectural overview

The code base follows a simple layered structure:

* **HTTP layer** – REST controllers receive JSON requests and return JSON responses.
* **Domain services** – business logic that creates, updates, and validates assets.
* **Infrastructure** – JPA repositories, MapStruct mappers, and schema/transform utilities that talk to the database and
  resource files.

### Main classes at a glance

| Component | Role |
| --- | --- |
| `AssetController` | Exposes `/assets` endpoints for create, bulk create, search, read, update, patch, bulk patch, and delete
  operations. It delegates every action to domain services and does not keep state by itself.【F:src/main/java/com/db/assetstore/infra/api/AssetController.java†L21-L119】 |
| `EventController` | Generates JSON events on demand. It loads an `Asset`, asks the `EventService` to run the right JSLT
  template, and streams the result back to the caller.【F:src/main/java/com/db/assetstore/infra/api/EventController.java†L15-L45】 |
| `AssetCommandServiceImpl` | Implements the command side of the system: creating assets, applying patches, deleting assets,
  and maintaining asset-to-asset links. It logs each successful command and performs soft deletes instead of removing rows
  outright.【F:src/main/java/com/db/assetstore/infra/service/AssetCommandServiceImpl.java†L43-L191】 |
| `AssetQueryServiceImpl` | Provides read access. It loads one asset by id or searches many assets using dynamically built JPA
  specifications.【F:src/main/java/com/db/assetstore/infra/service/AssetQueryServiceImpl.java†L17-L40】 |
| `TypeSchemaRegistry` | Scans the classpath for `schemas/types/*.schema.json`, compiles them with networknt JSON Schema, and
  remembers which `AssetType` values have a schema.【F:src/main/java/com/db/assetstore/domain/service/type/TypeSchemaRegistry.java†L21-L75】 |
| `AttributeDefinitionRegistry` | Reads the same schemas and builds a catalogue of attribute definitions (name, type, required
  flag) for each asset type.【F:src/main/java/com/db/assetstore/domain/service/type/AttributeDefinitionRegistry.java†L18-L119】 |
| `AttributeDefinitionsBootstrapService` | On application startup it stores missing attribute definitions in the database so the
  repository layer can enforce them later.【F:src/main/java/com/db/assetstore/domain/service/startup/AttributeDefinitionsBootstrapService.java†L16-L55】 |
| `StartupInfoService` | Listens for `ApplicationReadyEvent`, triggers the bootstrap step above, and logs the discovered asset
  types together with all available JSLT templates for visibility.【F:src/main/java/com/db/assetstore/domain/service/startup/StartupInfoService.java†L18-L68】 |
| `JsonTransformer` | Loads and caches `transforms/events/*.jslt`, applies them to JSON payloads, and validates the output JSON
  against optional schemas. Errors are logged with context to make template debugging easier.【F:src/main/java/com/db/assetstore/domain/service/transform/JsonTransformer.java†L22-L87】 |
| `JsonSchemaValidator` | Wraps the networknt validator. It parses JSON, compiles schemas, and turns validation failures into
  human-readable error messages.【F:src/main/java/com/db/assetstore/domain/service/validation/JsonSchemaValidator.java†L16-L85】 |
| `Asset` domain model | Represents an asset with core fields and a rich attribute collection. Helper methods make it easy to
  read and update attributes without exposing the internal collection details.【F:src/main/java/com/db/assetstore/domain/model/Asset.java†L21-L94】 |
| `AssetMapper` & `AttributeMapper` | Convert between domain objects and JPA entities. They take care of the soft-delete flag and
  transform typed attribute values into database-friendly shapes.【F:src/main/java/com/db/assetstore/infra/mapper/AssetMapper.java†L11-L36】【F:src/main/java/com/db/assetstore/infra/mapper/AttributeMapper.java†L15-L44】 |

## Functional overview

### What the system already allows

* **Create and update assets** – The `/assets` controller supports single and bulk creation, full updates, JSON Merge Patch, and
  soft deletion by id.【F:src/main/java/com/db/assetstore/infra/api/AssetController.java†L37-L118】【F:src/main/java/com/db/assetstore/infra/service/AssetCommandServiceImpl.java†L73-L191】
* **Search and read** – Clients can list all assets or fetch one asset by id. Searches use criteria objects that are translated
  into JPA specifications before hitting the database.【F:src/main/java/com/db/assetstore/infra/api/AssetController.java†L59-L73】【F:src/main/java/com/db/assetstore/infra/service/AssetQueryServiceImpl.java†L17-L40】【F:src/main/java/com/db/assetstore/infra/service/search/AssetSearchSpecificationService.java†L14-L45】
* **Generate events** – `/events/{assetId}/{eventName}` turns an asset into event JSON using the configured JSLT templates and
  optional schema checks.【F:src/main/java/com/db/assetstore/infra/api/EventController.java†L28-L41】【F:src/main/java/com/db/assetstore/domain/service/EventService.java†L17-L61】【F:src/main/java/com/db/assetstore/domain/service/transform/JsonTransformer.java†L32-L87】
* **Schema-driven validation** – Asset schemas are discovered at startup, translated into attribute definitions, persisted, and
  reused for validation and database integrity checks.【F:src/main/java/com/db/assetstore/domain/service/type/TypeSchemaRegistry.java†L35-L75】【F:src/main/java/com/db/assetstore/domain/service/type/AttributeDefinitionRegistry.java†L29-L119】【F:src/main/java/com/db/assetstore/domain/service/startup/AttributeDefinitionsBootstrapService.java†L25-L55】【F:src/main/java/com/db/assetstore/domain/service/validation/JsonSchemaValidator.java†L21-L85】
* **Typed attribute handling** – Assets keep attributes as typed objects (String, Decimal, Boolean). The mapper layer converts
  them to and from the database automatically, which keeps the domain logic clean.【F:src/main/java/com/db/assetstore/domain/model/Asset.java†L47-L94】【F:src/main/java/com/db/assetstore/infra/mapper/AttributeMapper.java†L15-L44】

### What still needs work

* **Pagination and richer filters** – `GET /assets` currently returns the full list without paging or query parameters, which may
  not scale for larger datasets. Extending the controller and search service with pagination and filters would improve usability
  and performance.【F:src/main/java/com/db/assetstore/infra/api/AssetController.java†L59-L65】【F:src/main/java/com/db/assetstore/infra/service/search/AssetSearchSpecificationService.java†L17-L33】
* **HTTP endpoints for asset links** – The command service already knows how to create and delete asset-to-asset links, but no
  controller triggers these visitor paths yet. Exposing them through dedicated endpoints would unlock relationship management via
  the API.【F:src/main/java/com/db/assetstore/infra/service/AssetCommandServiceImpl.java†L121-L143】【F:src/main/java/com/db/assetstore/infra/api/AssetController.java†L21-L119】
* **Event error handling** – When event generation fails (for example due to malformed templates), the API currently throws a
  generic runtime exception. Returning a structured error response would make debugging easier for clients.【F:src/main/java/com/db/assetstore/infra/api/EventController.java†L38-L42】

