# Asset Store PoC

This proof of concept shows how to store, check, and publish "assets" such as CRE or SHIP deals. It is a Spring Boot
service that depends on JSON Schemas and JSLT templates. New asset types or event payloads can be added by dropping new
files into the right folders instead of writing extra Java code.

## Architectural overview

The project follows a small layered layout.

- HTTP layer: REST controllers read JSON requests and return JSON responses.
- Domain services: this layer holds the business rules that create, update, and validate assets.
- Infrastructure: repositories, mappers, and helpers that talk to the database and to schema or transform files.

### Main classes

AssetController handles the `/assets` endpoints for single and bulk create, search, read, update, patch, bulk patch, and
delete. It calls the domain services for all work and keeps no state on its own.

EventController serves the `/events/{assetId}/{eventName}` endpoint. It loads an asset, asks the EventService to run the
right JSLT template, and streams the event JSON back to the caller.

AssetCommandServiceImpl carries out the command side of the system. It creates assets, applies patches, deletes assets,
and keeps the links between related assets. It logs successful commands and uses soft deletes.

AssetQueryServiceImpl offers read access. It can load one asset by id or search for many assets with dynamically built JPA
specifications.

TypeSchemaRegistry scans the classpath for `schemas/types/*.schema.json`, compiles them with the networknt JSON Schema
library, and tracks which AssetType values have a schema.

AttributeDefinitionRegistry reads the same schemas and builds a catalogue of attribute definitions (name, type, required
flag) for each asset type.

AttributeDefinitionsBootstrapService runs at startup. It stores missing attribute definitions in the database so the
repository layer can enforce them later.

StartupInfoService listens for `ApplicationReadyEvent`, triggers the bootstrap step above, and logs the discovered asset
types along with the available JSLT templates.

JsonTransformer loads and caches `transforms/events/*.jslt`, applies them to JSON payloads, and checks the output JSON
against optional schemas. It logs detailed errors to help when templates fail.

JsonSchemaValidator wraps the networknt validator. It parses JSON, compiles schemas, and turns validation failures into
clear error messages.

Asset is the main domain model. It represents an asset with core fields and a rich attribute collection. Helper methods
make it easy to read and update attributes without exposing the internal collection details.

AssetMapper and AttributeMapper convert between domain objects and JPA entities. They manage the soft delete flag and turn
typed attribute values into shapes that are easy to store in the database.

## Functional overview

### What the system already allows

- Create and update assets. The `/assets` controller supports single and bulk creation, full updates, JSON Merge Patch,
  and soft deletion by id.
- Search and read. Clients can list all assets or fetch one asset by id. Searches use criteria objects that are translated
  into JPA specifications before touching the database.
- Generate events. The `/events/{assetId}/{eventName}` endpoint turns an asset into event JSON using the configured JSLT
  templates and optional schema checks.
- Schema driven validation. Asset schemas are discovered at startup, translated into attribute definitions, persisted, and
  reused for validation and database integrity checks.
- Typed attribute handling. Assets keep attributes as typed objects such as string, decimal, or boolean. The mapper layer
  converts them to and from the database automatically, which keeps the domain logic clean.

### What still needs work

- Pagination and richer filters. `GET /assets` currently returns the full list without paging or query parameters, which
  will not scale for large datasets. Extending the controller and search service with pagination and filters would improve
  usability and performance.
- HTTP endpoints for asset links. The command service already knows how to create and delete links between assets, but no
  controller exposes these paths yet. Adding endpoints would unlock relationship management through the API.
- Event error handling. When event generation fails, the API throws a generic runtime exception. A structured error
  response would make troubleshooting easier for clients.

