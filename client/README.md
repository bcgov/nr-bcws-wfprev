WFPREV Client v1.0.0

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The application will automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via a platform of your choice. To use this command, you need to first add a package that implements end-to-end testing capabilities.

## Maps and basemaps

| Basemap (as shown) | Id | Source | Notes |
|---|---|---|---|
| Topographic | `topographic-v2` | Esri World Hillshade + [ESRI Topographic](https://www.arcgis.com/home/item.html?id=67372ff42cd145319639a99152b15bc3) vector tiles | **Default** |
| BC (Hillshade) | `bc-basemap-hillshade` | [BC Basemap, with hillshade](https://governmentofbc.maps.arcgis.com/home/item.html?id=bbe05270d3a642f5b62203d6c454f457) vector tiles | Includes Forest Service Roads |
| Imagery | `imagery-v2` | Esri World Imagery + Esri Canada imagery labels | |

Every map is an SMK map, and offers exactly these three, with the same names. SMK already defines these
basemaps. The default and the switcher entries are configured in `services/map-config.service/map.config.ts`.
The switcher is SMK's `baseMaps` tool (`baseMapsToolConfig()`):

- SMK ships the tool disabled, so the code turns it on with `enabled: true`.
- `choices` limits it to the approved ids.
- Its panel shows the static previews in `src/assets/basemaps/`, not live mini maps. They are stacked in one
  column by a global rule in `styles.scss`, and the side panel narrows to their width while they are shown.

The maps:

- **Main map** (`components/map`): created by `MapService.createSMK()`, with the layers, identify and
  full-extent tools.
- **Mini maps** (project details, fiscal map, spatial file viewer): created by `MiniMapService.create()` from
  `miniMapConfig()`. They have the basemap switcher, panning and zoom buttons, and nothing else. They keep the
  mini maps' zoom limit of 18 and have none of the main map's pan bounds.
  - `create()` resolves once SMK has shown the map, about a second later. The components draw what loaded in
    the meantime then.
  - Everything drawn on a mini map is built with `MiniMap.leaflet`, SMK's own copy of Leaflet, not the app's
    `leaflet` module. The two copies number their layers separately, so their ids can clash on one map, and
    neither accepts the other's `LatLngBounds`.
  - The project details map may be created while its tab isn't showing. `create()` undoes the two things that
    breaks, and the page re-fits the map when the tab opens (`refreshMap()`).

To change the list, edit `APPROVED_BASEMAPS` / `DEFAULT_BASEMAP_ID` in `utils/constants.ts`. When adding a
basemap:

1. Use an id from SMK's base-map registry. Every map picks it up from there.
2. Add a 220×220 PNG preview (shown at 110 px) to `src/assets/basemaps/`. The existing previews are centred on
   Whistler (50.12, -122.95) at MapLibre zoom 9.

### How SMK is loaded

`smk.js` is a global script (`angular.json`), but SMK only loads its Leaflet viewer, plugins and basemap
registry on first use.

- **`SmkService.load()` loads them once per session**, through SMK's own module loader (`window.include`).
  Every map shares that one load, and no map has to be created for it.
- **`AppComponent` calls `SmkService.preload()`**, which runs the load while the browser is idle, so the first
  map doesn't wait for it.
- **`MapService.patch()` installs our SMK patches once per session**, after the load. These are the
  full-extent tool, authenticated WMS legends, and the viewer's max bounds and max zoom.
- **Every map is created through `MapService.initSMK()`.** SMK creates maps one at a time, and once one fails
  to initialize it fails every later one too. `initSMK()` stops an earlier failure from carrying over. Mini
  maps also get their own ids, since SMK's default id repeats once a map is destroyed.
- **The main map is kept alive between visits.** When you leave the map page, `MapComponent` removes its own
  markers, boundaries and legend. `MapService.detachSMK()` then parks the SMK map off screen. On the next
  visit, `reattachSMK()` puts it back, with its basemap, layers and view, instead of building a new map.
  If the auth token has changed since the map's WMS layers were created, the map is rebuilt instead.

SMK is pinned to exactly `@qqnluaq/smk` `1.0.39`, the release that introduced these basemaps. Do not install
`@latest`: that dist-tag points at `1.0.16100`, which is an older release line without them.

**Note:** Esri's Topographic attribution reads "… © OpenStreetMap contributors …". This credits OSM as one of
Esri's data sources; it is not an OSM tile source. Esri's terms require the text to be shown.

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI Overview and Command Reference](https://angular.io/cli) page.
