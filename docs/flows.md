# Przepływy (flows) i użyte elementy

Poniżej znajdują się rozrysowane (Mermaid) kluczowe przepływy systemu oraz lista komponentów używanych na każdym etapie. Diagramy możesz otworzyć w IDE z podglądem Mermaid lub w GitHub/VS Code, które wspierają Mermaid.

Jeśli preferujesz PlantUML, zobacz odpowiedniki diagramów w pliku: docs/flows-plantuml.puml (możesz go wyrenderować w dowolnym pluginie/rendererze PlantUML).

## 1) Start aplikacji (bootstrap + informacje)

```mermaid
flowchart TD
    A[Spring Boot start] --> B[TypeSchemaRegistry.discover()]:::svc
    B --> C[AttributeDefinitionRegistry.rebuild()]:::svc
    A --> D[ApplicationReadyEvent]
    D --> E[StartupInfoService.onReady()]:::svc
    E --> F[AttributeDefinitionsBootstrapService.bootstrap()]:::svc
    F -->|JPA| G[(DB) AttributeDefEntity]:::db
    E --> H[Skany zasobów transforms/**/*.jslt]:::io
    E --> I[Log: wspierane typy i transformacje]:::log

classDef svc fill:#e7f3ff,stroke:#3b82f6,color:#111;
classDef db fill:#fef3c7,stroke:#f59e0b,color:#111;
classDef io fill:#ecfccb,stroke:#84cc16,color:#111;
classDef log fill:#f3e8ff,stroke:#a855f7,color:#111;
```

Użyte elementy:
- TypeSchemaRegistry: wykrywa schematy typów z classpath (schemas/{TYPE}.schema.json)
- AttributeDefinitionRegistry: buduje definicje atrybutów (properties/required)
- AttributeDefinitionsBootstrapService: zapisuje brakujące definicje do DB (AttributeDefEntity)
- StartupInfoService: orchestracja na ApplicationReadyEvent + logowanie i skan transformacji
- DB: AttributeDefEntity tabela z definicjami atrybutów

## 2) Tworzenie aktywa z koperty {type, id, ...}

```mermaid
flowchart TD
    A[HTTP POST /assets]:::http --> B[AssetController]:::web
    B --> C[DefaultAssetService.addAssetFromJson]:::svc
    C --> D[AssetAttributeValidationService.validateEnvelope]:::svc
    D --> E[AssetJsonFactory.fromJson]:::json
    D --> F[AssetTypeValidator.ensureSupported + validateAttributes]:::val
    F --> G[TypeSchemaRegistry.getSchemaPath]:::svc
    F --> H[JsonSchemaValidator.validateIfPresent]:::val
    C --> I[repository.saveAsset]:::repo
    I --> J[JpaAssetRepository]:::repo
    J --> K[(DB) save Asset + AttributeEntity + History]:::db

classDef http fill:#e0f2fe,stroke:#0284c7,color:#111;
classDef web fill:#f1f5f9,stroke:#64748b,color:#111;
classDef svc fill:#e7f3ff,stroke:#3b82f6,color:#111;
classDef json fill:#ecfeff,stroke:#06b6d4,color:#111;
classDef val fill:#e9ffe7,stroke:#16a34a,color:#111;
classDef repo fill:#fff7ed,stroke:#ea580c,color:#111;
classDef db fill:#fef3c7,stroke:#f59e0b,color:#111;
```

Użyte elementy:
- AssetController (warstwa web)
- DefaultAssetService (orkiestracja)
- AssetAttributeValidationService + AssetTypeValidator (weryfikacja typu i atrybutów)
- TypeSchemaRegistry + JsonSchemaValidator (walidacja JSON Schema – best-effort)
- AssetJsonFactory (parsowanie JSON do modelu Asset + atrybuty)
- AssetRepository/JpaAssetRepository (zapis i kontrola spójności względem definicji z DB)
- DB: AssetEntity, AttributeEntity, AttributeHistoryEntity

## 3) Tworzenie aktywa z JSON typu (bez koperty)

```mermaid
flowchart TD
    A[HTTP POST /assets/{type}]:::http --> B[AssetController]:::web
    B --> C[DefaultAssetService.addAssetFromJson(type,json)]:::svc
    C --> D[AssetAttributeValidationService.validateForType]:::svc
    D --> E[AssetJsonFactory.fromJsonForType]:::json
    D --> F[AssetTypeValidator.ensureSupported + validateAttributes]:::val
    F --> G[TypeSchemaRegistry.getSchemaPath]:::svc
    F --> H[JsonSchemaValidator.validateIfPresent]:::val
    C --> I[repository.saveAsset]:::repo
    I --> J[JpaAssetRepository]:::repo
    J --> K[(DB)]:::db
```

Użyte elementy: jak wyżej; różnica polega na formacie wejścia i sposobie wyłuskania atrybutów po typie.

## 4) Generowanie zdarzenia (EventService + JSLT)

```mermaid
flowchart TD
    A[EventService.generate(eventName, asset)]:::svc --> B[AssetCanonicalizer.toCanonicalJson]:::json
    B --> C[Zbuduj kontekst {eventName, occurredAt, asset}]:::json
    C --> D[JsonTransformer.transform("events/"+eventName)]:::svc
    D --> E[Parser.compile + apply JSLT]:::jslt
    E --> F[(wynik JSON)]:::io
    D --> G[Opcj. walidacja: schemas/transforms/*.schema.json lub schemas/events/*.schema.json]:::val

classDef svc fill:#e7f3ff,stroke:#3b82f6,color:#111;
classDef json fill:#ecfeff,stroke:#06b6d4,color:#111;
classDef jslt fill:#fef9c3,stroke:#ca8a04,color:#111;
classDef io fill:#ecfccb,stroke:#84cc16,color:#111;
classDef val fill:#e9ffe7,stroke:#16a34a,color:#111;
```

Użyte elementy:
- AssetCanonicalizer: kanonizacja modelu Asset do JSON
- JsonTransformer: wykonanie szablonu JSLT (transforms/events/{event}.jslt); cache wyrażeń
- (Opcjonalnie) JsonSchemaValidator: walidacja wyniku jeśli istnieje odpowiadający schema plik

## 5) Zależności i katalogi zasobów
- Schematy typów: src/main/resources/schemas/{TYPE}.schema.json
- Schematy wyników transformacji: src/main/resources/schemas/transforms/*.schema.json i/lub src/main/resources/schemas/events/*.schema.json
- Szablony JSLT: src/main/resources/transforms/**/*.jslt

## 6) Uwagi
- Walidacja schematów: best-effort (brak pliku schematu nie blokuje przepływu); błędy typu/required zgłaszają wyjątek.
- Repozytorium JPA stanowi ostatnią linię spójności danych (wymusza definicje i required w DB).
